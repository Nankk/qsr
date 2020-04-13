;;; 命名、全然commonなんかじゃないってことは分かりながらもglobalでもないから困ったのだ
(ns qsr.common
  (:require
   [cljs.core.async :as async :refer [>! <! chan go timeout]]
   [cljs-http.client :as http]
   [re-frame.core :as rf]
   ["sortablejs" :as Sortable]
   [qsr.const :as const]
   [qsr.subs :as subs]
   [qsr.events :as events]
   ))

(defn refresh []
  (println "refresh")
  (go (let [json (<! (http/get (str const/server-url "/get-image-list")
                               {:with-credentials? false}))
            list            (get-in json [:body :img-list])]
        (rf/dispatch-sync [::events/update-img-list list]))))

(defn upload-image-list []
  (println "upload-image-list")
  (go (let [res (<! (http/post (str const/server-url "/upload-image-list")
                               {:with-credentials? false
                                :json-params       @(rf/subscribe [::subs/img-list])}))]
        (. js/console log res))))

(defn sortable-setup []
  ;; This function must be re-loaded in the develop build *after* reagent re-render
  (println "sortable-setup loaded.")
  (Sortable. (.getElementById js/document "image-list")
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
