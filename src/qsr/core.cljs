(ns qsr.core
  (:require
   [reagent.dom :as reagent.dom]
   [re-frame.core :as rf]
   [qsr.events :as events]
   [qsr.views :as views]
   [qsr.config :as config]
   [qsr.styles.core :as styles.core]
   [qsr.common :as common]))

(defn- compile-garden []
  (println "Compiling garden...")
  (let [css-text (styles.core/summarize)
        css-elem (. js/document getElementById "garden")]
    (set! (. css-elem -textContent) css-text)))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (compile-garden)
  (rf/clear-subscription-cache!)
  (reagent.dom/render [views/main-panel]
                      (.getElementById js/document "app"))
  (common/sortable-setup))

(defn init []
  (rf/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
