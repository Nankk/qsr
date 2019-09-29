(ns qsr.views
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [qsr.subs :as subs]
   [qsr.events :as events]
   [qsr.db :as db]
   [clojure.string :as str]
   [qsr.gapis :as gapis]
   [cljs-http.client :as http]))

;; Item panel ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn post-request [request]
  (println "post-request called")
  (println request)
  (http/post request))

(defn on-item-click [item]
  ;; (re-frame/dispatch-sync [::events/select-item item])
  (let [api-url "https://vrcpanorama-get-image.herokuapp.com/index.php"
        req-url (str api-url "?type=move&page=" (item :index))]
    (post-request req-url)))

(defn item-card [item]
  [:button {:class "transparent" :on-click #(on-item-click item)}
   [:div {:class "card"}
    [:img {:class "card-img-top" :style {:width "100%" :object-fit "cover"} :src (item :url) :alt (item :name)}]
    [:div {:class "card-body"}
     [:p (str "Index: " (item :index))]
     [:p (str "Drive ID:" (item :id))]]]]
  )

(defn item-list-row [pair] ; TODO: change to accept arbitrary number of items
  (println (first pair))
  [:div.row
   [:div {:class "col-sm-6"} [item-card (first pair)]]
   (when (some? (second pair))
     [:div {:class "col-sm-6"} [item-card (second pair)]])])

(defn item-list []
  [:div.container
   (let [items @(re-frame/subscribe [::subs/items])
         pairs (loop [queue items
                      item-pairs []]
                 (println (str "queue: " queue))
                 (println (str "item-pairs: " item-pairs))
                 (if (not-empty queue)
                   (recur (if (= (count queue) 1)
                            (conj queue nil)
                            (subvec queue 2))
                          (conj item-pairs [(first queue) (second queue)]))
                   item-pairs))]
     (println pairs)
     (for [pair pairs]
       [item-list-row pair]))])

;; Main ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn main-panel []
  (let [refresh-fn #(gapis/get-values-from-sheet
                     "1vkNkO71CfPhft-gRYFkvTwtg23-O75Dyaq0IIiF_-Dg"
                     "Default!A:A")]
    (refresh-fn)
    [:div.large-container
     [:h1 "Hi I am Main Panel."]
     [:button {:class "btn btn-primary"
               :on-click refresh-fn}
      [:i {:class "fas fa-sync-alt" :aria-hidden true}]
      " Refresh"]
     [item-list]]))



