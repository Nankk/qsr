(ns qsr.views
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [qsr.subs :as subs]
   [qsr.events :as events]
   [qsr.db :as db]
   [clojure.string :as str]
   [qsr.gapis :as gapis]
   [cljs-http.client :as http]
   [cljs.core.async :as async :refer [chan go go-loop >! <!]]))

;; Item panel ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn on-item-click [item]
  (re-frame/dispatch-sync [::events/select-item item])
  (let [api-url "https://vrcpanorama-get-image.herokuapp.com/index.php"
        req-url (str api-url "?type=move&page=" (item :index))]
    (go (http/get req-url))))

(defn item-card [item]
  [:button {:class "transparent" :on-click #(on-item-click item)}
   [:div {:class (str "card hoverable" (when (item :selected?) " selected"))}
    [:img {:data-sizes "auto"
           :data-src (item :url)
           :class "card-img-top lazyload"}]
    [:div {:class "card-body"}
     (str "Index: " (item :index))
     [:br]
     (str "Name: " (item :name))]]])

(defn item-list-row [pair] ; TODO: change to accept arbitrary number of items
  [:div.row
   [:div {:class "col-md-6"} [item-card (first pair)]]
   (when (some? (second pair))
     [:div {:class "col-md-6"} [item-card (second pair)]])])

(defn item-list []
  [:div
   (let [items @(re-frame/subscribe [::subs/items])
         pairs (loop [queue items
                      item-pairs []]
                 (if (not-empty queue)
                   (let [new-queue (if (= (count queue) 1)
                                     (conj queue nil)
                                     (subvec queue 2))]
                     (recur (if (= (count queue) 1)
                              []
                              (subvec queue 2))
                            (conj item-pairs [(first queue) (second queue)])))
                   item-pairs))]
     (for [pair pairs]
       ^{:key pair} [item-list-row pair]))])

;; Main ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn greeting []
  (let [hr-now (. (js/Date.) getHours)]
    (cond
      (< hr-now 5) "Hi, hard-worker ;-)"
      (= hr-now 5) "The sun is raising..."
      (< 5 hr-now 11) "Good morning Sir/Ma'am, how are you doing?"
      (= hr-now 11) "Lunch is around the corner."
      (< 11 hr-now 18) "Hello Sir/Ma'am, how are you doing?"
      (= hr-now 18) "Shall we take a break?"
      (< 18 hr-now 24) "Good evening, Sir/Ma'am."
      (= hr-now 24) "You'd better go to bed for tommorow, right?")))

(def gapi-ch (chan))

(defn formatted-sheets-data [vs]
  (let [vs-sub (subvec vs 3)]
    (into [] (map-indexed (fn [i v]
                            (let [url-raw (first v)
                                  img-id (second (re-matches #".*file/d/([^/]+).*" url-raw))]
                              {:id img-id
                               :index i
                               :url (str "https://drive.google.com/uc?export=view&id=" img-id)}))
                          vs-sub))))

(defn formatted-drive-data [vs]
  (into [] (for [v vs]
             {:id   (get v "id")
              :name (get v "name")})))

(defn items [rs]
  (let [s-vs (formatted-sheets-data (rs :sheets))
        d-vs (formatted-drive-data (rs :drive))]
    (into [] (for [s-v s-vs]
               (let [idx (first (keep-indexed (fn [i v] (when (= (v :id) (s-v :id)) i)) d-vs))]
                 (assoc s-v :name (get-in d-vs [idx :name])))))))

(go-loop [rs {}]
  (when-let [r (<! gapi-ch)]
    (let [new-rs (assoc rs (r :type) (r :content))]
      (if (and (contains? new-rs :drive)
               (contains? new-rs :sheets))
        (do (println "gonna set-items!")
            (re-frame/dispatch-sync [::events/set-items (items new-rs)])
            (recur {}))
        (recur new-rs)))))

(defn refresh []
  (gapis/get-values-from-sheet
   "1vkNkO71CfPhft-gRYFkvTwtg23-O75Dyaq0IIiF_-Dg"
   "Default!A:A"
   (fn [err res]
     (when err (throw err))
     (. js/console log "Got values from sheet.")
     (let [vs (js->clj (.. res -data -values))]
       (go (>! gapi-ch {:type :sheets :content vs})))))
  (gapis/get-items-in-directory
   "1V86RuISEWxMeg8vIuKz190oBNEJIxNq0"
   1000
   "files(name, id)"
   (fn [err res]
     (when err (throw err))
     (. js/console log "Got items in directory.")
     (let [vs (js->clj (.. res -data -files))]
       (go (>! gapi-ch {:type :drive :content vs}))))))

(defn main-panel []
  (refresh)
  [:div.container
   [:h3 (greeting)]
   [:button {:class "btn btn-primary"
             :on-click #(refresh)}
    [:i {:class "fas fa-sync-alt" :aria-hidden true}]
    " Sync"]
   [item-list]])

