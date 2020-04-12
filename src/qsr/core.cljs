(ns qsr.core
  (:require
   [reagent.dom :as reagent.dom]
   [re-frame.core :as rf]
   [qsr.events :as events]
   [qsr.views :as views]
   [qsr.config :as config]
   [qsr.const :as const]
   [qsr.subs :as subs]
   [cljs.core.async :as async :refer [>! <! chan go timeout]]
   [cljs-http.client :as http]
   ["sortablejs" :as Sortable]
   [qsr.styles.core :as styles.core]
   [clojure.string :as str]))

(defn- compile-garden []
  (println "Compiling garden...")
  (let [css-text (styles.core/summarize)
        css-elem (. js/document getElementById "garden")]
    (set! (. css-elem -textContent) css-text)))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn- upload-image-list []
  (println "upload-image-list")
  (go (let [res (<! (http/post (str const/server-url "/upload-image-list")
                               {:with-credentials? false
                                :json-params       @(rf/subscribe [::subs/img-list])}))]
        (. js/console log res)
        )))

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
                                  (rf/dispatch-sync [::events/on-manually-sorted from-to])
                                  (upload-image-list)))})))

(defn ^:dev/after-load mount-root []
  (compile-garden)
  (rf/clear-subscription-cache!)
  (reagent.dom/render [views/main-panel]
                      (.getElementById js/document "app"))
  (sortable-setup))

(defn init []
  (rf/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root)
  )
