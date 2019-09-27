(ns qsr.views
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [qsr.subs :as subs]
   [qsr.events :as events]
   [qsr.db :as db]
   [clojure.string :as str]
   [qsr.gapis :as gapis]))

;; Main ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn main-panel []
  [:div.large-container
   [:h1 "Hi I am Main Panel."]
   [:div {:class "btn btn-primary"
          :on-click #(gapis/get-values-from-sheet
                      "1vkNkO71CfPhft-gRYFkvTwtg23-O75Dyaq0IIiF_-Dg"
                      "Default!A:A")}
    "DANGER NEVER CLICK THIS"]])

