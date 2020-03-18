(ns qsr.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as rf]
   [qsr.events :as events]
   [qsr.views :as views]
   [qsr.config :as config]
   ["sortablejs" :as Sortable]
   [qsr.style.core :as style.core]))

(defn- compile-garden []
  (println "Compiling garden...")
  (let [css-text (style.core/summarize)
        css-elem (. js/document getElementById "garden")]
    (set! (. css-elem -textContent) css-text)))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn sortable-setup []
  ;; This function must be re-loaded in the develop build *after* reagent re-render
  (println "sortable-setup loaded.")
  (Sortable. (.getElementById js/document "item-list")
             (clj->js {:group "g1"
                       :animation 50
                       :delay 150
                       :delayOnTouchOnly true
                       :onEnd (fn [e]
                                (let [old (. e -oldIndex)
                                      new (. e -newIndex)
                                      from-to [old new]]
                                  (rf/dispatch-sync [::events/on-manually-sorted from-to])))})))

(defn ^:dev/after-load mount-root []
  (compile-garden)
  (rf/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app"))
  (sortable-setup))

(defn init []
  (rf/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root)
  )
