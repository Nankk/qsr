(ns qsr.views
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [qsr.subs :as subs]
   [qsr.events :as events]
   [qsr.db :as db]
   [clojure.string :as str]))

;; Main ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn main-panel []
  (let [name @(re-frame/subscribe [::subs/name])]
    [:h1 (str "Hi, " name)]))

