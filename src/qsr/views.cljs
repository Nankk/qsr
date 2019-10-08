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
   [cljs.core.async :as async :refer [chan go go-loop >! <!]]
   [qsr.const :as const]))

;; Item panel ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn on-item-click [item]
  (re-frame/dispatch-sync [::events/select-item item])
  (let [api-url "https://vrcpanorama-get-image.herokuapp.com/index.php"
        req-url (str api-url "?type=move&page=" (item :sheet-idx))]
    (go (http/get req-url))))

(defn item-card [item]
  [:li
   [:button {:class "transparent" :on-click #(on-item-click item)}
    [:div {:class (str "card hoverable" (when (item :selected?) " selected"))}
     [:img {:data-sizes "auto"
            :data-src (item :url)
            :class "card-img-top lazyload"}]
     [:div {:class "card-body"}
      (str "sheet index: " (item :sheet-idx))
      [:br]
      (str "name: " (item :name))]]]])

(defn item-list []
  [:ul {:id "item-list"
        :class "wrap-list"}
   (let [items @(re-frame/subscribe [::subs/items])]
     (for [item items]
       ^{:key item} [item-card item]))])

;; Main ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def gapi-ch (chan))

(defn formatted-sheets-data [vs]
  (let [vs-sub (subvec vs 3)]
    (into [] (map-indexed (fn [i v]
                            (let [url-raw (first v)
                                  img-id (second (re-matches #".*file/d/([^/]+).*" url-raw))]
                              {:id img-id
                               :sheet-idx i
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
     (. js/console log "got values from sheet.")
     (let [vs (js->clj (.. res -data -values))]
       (go (>! gapi-ch {:type :sheets :content vs})))))
  (gapis/get-items-in-directory
   "1V86RuISEWxMeg8vIuKz190oBNEJIxNq0"
   1000
   "files(name, id)"
   (fn [err res]
     (when err (throw err))
     (. js/console log "got items in directory.")
     (let [vs (js->clj (.. res -data -files))]
       (go (>! gapi-ch {:type :drive :content vs}))))))

(defn dropdown-sort-by []
  [:span {:class "dropdown"}
   [:button {:class         "btn btn-light dropdown-toggle"
             :data-toggle   "dropdown"
             :aria-haspopup true
             :aria-expanded false}
    (name @(re-frame/subscribe [::subs/sort-by]))]
   [:div {:class           "dropdown-menu"
          :aria-labelledby "dropdown1"}
    (for [by [:sheet-idx :name]]
      [:button {:key      by
                :class    "dropdown-item"
                :on-click (fn [_]
                            (re-frame/dispatch-sync [::events/set-sort-by by])
                            (re-frame/dispatch-sync [::events/sort-items]))}
       (name by)])]])

(defn dropdown-sort-order []
  [:span {:class "dropdown"}
   [:button {:class         "btn btn-light dropdown-toggle"
             :data-toggle   "dropdown"
             :aria-haspopup true
             :aria-expanded false}
    (name @(re-frame/subscribe [::subs/sort-order]))]
   [:div {:class           "dropdown-menu"
          :aria-labelledby "dropdown2"}
    (for [order [:ascending :descending]]
      [:button {:key      order
                :class    "dropdown-item"
                :on-click (fn [_]
                            (re-frame/dispatch-sync [::events/set-sort-order order])
                            (re-frame/dispatch-sync [::events/sort-items]))}
       (name order)])]])

(defn main-panel []
  (refresh)
  [:div {:class "container"}
   [:h3 {:style {:text-align "center"}} (const/random-word)]
   [:div {:class "card"}
    [:div {:class "card-body"}
     [:button {:class "btn btn-primary"
               :on-click #(refresh)}
      [:i {:class "fas fa-sync-alt" :aria-hidden true}]
      "　Reload sheet data"]]
    [:div {:class "card-body"}
     "Sort by　"
     [dropdown-sort-by]
     "　in　"
     [dropdown-sort-order]
     "　order"]]
   [item-list]])

