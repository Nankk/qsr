(ns qsr.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [qsr.events :as events]
   [qsr.views :as views]
   [qsr.config :as config]
   ["sortablejs" :as Sortable]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn sortable-init []
  (Sortable. (.getElementById js/document "item-list")
             (clj->js {:group "g1"
                       :animation 200
                       :delay 200
                       :delay-on-touch-only true
                       :on-end (fn [e]
                                (let [old (. e -oldIndex)
                                      new (. e -newIndex)
                                      from-to [old new]]
                                  (re-frame/dispatch-sync [::events/on-manually-sorted from-to])))})))

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root)
  (sortable-init))
