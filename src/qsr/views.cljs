(ns qsr.views
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [qsr.subs :as subs]
   [qsr.events :as events]
   [qsr.db :as db]
   [clojure.string :as str]
   [qsr.gapis :as gapis]))

;; Item panel ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn item-card [item]
  [:div {:class "card" :margin "20px"}
   [:img {:class "card-img-top" :style {:width "100%" :object-fit "cover"} :src (item :url) :alt (item :name)}]
   [:div {:class "card-body"}
    [:p (str "Index: " (item :index))]
    [:p (str "Drive ID:" (item :id))]]])

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
  [:div.large-container
   [:h1 "Hi I am Main Panel."]
   [:button {:class "btn btn-primary"
             :on-click #(gapis/get-values-from-sheet
                         "1vkNkO71CfPhft-gRYFkvTwtg23-O75Dyaq0IIiF_-Dg"
                         "Default!A:A")}
    [:i {:class "fas fa-sync-alt" :aria-hidden true}]
    " Refresh"]
   [item-list]])



