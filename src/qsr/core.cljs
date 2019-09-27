(ns qsr.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [qsr.events :as events]
   [qsr.views :as views]
   [qsr.config :as config]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
