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
   [cljs.core.async :as async :refer [go >! <!]]))

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
     [:p (str "Index: " (item :index))]]]])

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
(defn main-panel []
  (let [refresh-fn #(gapis/get-values-from-sheet
                     "1vkNkO71CfPhft-gRYFkvTwtg23-O75Dyaq0IIiF_-Dg"
                     "Default!A:A")]
    (refresh-fn)
    [:div.container
     [:h3 (greeting)]
     [:button {:class "btn btn-primary"
               :on-click refresh-fn}
      [:i {:class "fas fa-sync-alt" :aria-hidden true}]
      " Sync"]
     [item-list]]))



