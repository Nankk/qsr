(ns qsr.views
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [qsr.subs :as subs]
   [qsr.events :as events]
   [qsr.db :as db]
   [clojure.string :as str]
   [qsr.gapis :as gapis]))

(defn item-card [item]
  [:div {:class "card"}
   [:img {:class "card-img-top" :src (item :url) :alt (item :name)}]
   [:div {:class "card-body"}
    [:p (item :name)]]])

(defn item-list []
  (let [items @(re-frame/subscribe [::subs/items])]
    [:div (for [item items]
            ^{:key item} [item-card item])]))

;; Main ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn main-panel []
  [:div.large-container
   [:h1 "Hi I am Main Panel."]
   [:div {:class "btn btn-warning"
          :on-click #(gapis/get-values-from-sheet
                      "1vkNkO71CfPhft-gRYFkvTwtg23-O75Dyaq0IIiF_-Dg"
                      "Default!A:A")}
    "DANGER NEVER CLICK THIS"]
   [item-list]])



