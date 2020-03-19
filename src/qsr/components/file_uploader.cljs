(ns qsr.components.file-uploader
  (:require
   [qsr.events :as events]
   [qsr.subs :as subs]
   [re-frame.core :as rf]
   [cljs.core.async :as async :refer [>! <! chan go timeout]]
   [cljs-http.client :as http]
   [qsr.gapis :as gapis]
   [qsr.const :as const]
   ))

;; File uploader ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def drop-area-id "drop-area")

;; for now just showing dropped items to the console

(defn handle-drag-over [e]
  (. e stopPropagation)
  (. e preventDefault)
  (set! (.. e -dataTransfer -dropEffect) "copy"))

(defn upload-files [files]
  (doseq [file files]
    (gapis/drive-upload "1V86RuISEWxMeg8vIuKz190oBNEJIxNq0" file #())))

(defn main []
  [:div.container
   [:div.row
    [:div.col-sm-2]
    [:div.col-sm-8
     [:div.card
      [:div.card-body
       [:div.sink-zone {:id drop-area-id
                        :on-drop (fn [e]
                                   (. e stopPropagation)
                                   (. e preventDefault)
                                   (let [files (.. e -dataTransfer -files)]
                                     (. js/console log files)))
                        :on-drag-over #(handle-drag-over %)}
        ;; Icon & caption
        [:div
         [:i.fas.fa-cloud-upload-alt {:aria-hidden true
                                      :style {:font-size "6rem"
                                              :color "#0095F3"}}]]
        [:p "Drop files here," [:br] "or select files to upload"]
        ;; Hidden input element
        [:input.hidden-input {:id "file-selector"
                              :type "file"
                              :name "files[]"
                              :multiple "Is a dummy text OK...?"
                              :on-change (fn [e]
                                           (let [files (.. e -target -files)]
                                             (. js/console log files)))}]
        ;; Default style of input elements is ugly so using a label instead
        [:label {:for "file-selector"} "Browse files"]]]]
     [:div.col-sm-2]]]])
