(ns qsr.components.file-uploader
  (:require
   [qsr.events :as events]
   [qsr.subs :as subs]
   [re-frame.core :as rf]
   [cljs.core.async :as async :refer [>! <! chan go timeout]]
   [async-interop.interop :refer-macros [<p!]]
   [cljs-http.client :as http]
   ["uuid" :as uuidv4]
   [qsr.const :as const]))

;; File uploader ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def drop-area-id "drop-area")

(defn handle-drag-over [e]
  (. e stopPropagation)
  (. e preventDefault)
  (set! (.. e -dataTransfer -dropEffect) "copy"))

(defn- go-upload-file [url file]
  (println "go-upload-file")
  (go (try
        (let [name     (. file -name)
              data-uri (<p! (js/toDataURLPromise file))
              id       (uuidv4)
              _        (rf/dispatch-sync [::events/set-uploading? true])
              _        (rf/dispatch-sync [::events/add-upload-item {:name name :id id :done? false}])
              res      (<! (http/post url {:with-credentials? false
                                           :json-params       {:name name :data-uri data-uri}}))
              _        (. js/console log res)
              _        (rf/dispatch-sync [::events/complete-item-upload id])
              q        @(rf/subscribe [::subs/upload-queue])]
          (when (every? #(% :done?) q)
            (rf/dispatch-sync [::events/finish-uploading])
            (rf/dispatch-sync [::events/clear-upload-queue])))
        (catch js/Object e
          (. js/console log e)))))

(defn upload-files [files]
  (println "upload-files")
  (println files)
  (doseq [file files]
    (go-upload-file (str const/server-url "/upload-image") file)))

(defn main []
  [:div.container
   [:div.row
    [:div.col-sm-2]
    [:div.col-sm-8
     [:div.card
      [:div.card-body
       [:div.sink-zone {:id           drop-area-id
                        :on-drop      (fn [e]
                                        (. e stopPropagation)
                                        (. e preventDefault)
                                        (let [files (js->clj (. js/Array from (.. e -dataTransfer -files)))]
                                          (upload-files files)))
                        :on-drag-over #(handle-drag-over %)}
        ;; Icon & caption
        [:div
         [:i.fas.fa-cloud-upload-alt {:aria-hidden true
                                      :style       {:font-size "6rem"
                                                    :color     "#0095F3"}}]]
        [:p "Drop files here," [:br] "or select files to upload"]
        ;; Hidden input element
        [:input.hidden-input {:id        "file-selector"
                              :type      "file"
                              :name      "files[]"
                              :multiple  "Is a dummy text OK...?"
                              :on-change (fn [e]
                                           (let [files (.. e -target -files)]
                                             (. js/console log files)))}]
        ;; Default style of input elements is ugly so using a label instead
        [:label {:for "file-selector"} "Browse files"]]]]
     [:div.opacity-wrapper {:style {:opacity (if @(rf/subscribe [::subs/uploading?]) 1 0)}}
      [:div.card {:style {:padding "0px 20px"}}
       [:div.card-body
        (let [q     @(rf/subscribe [::subs/upload-queue])
              total (count q)
              done  (count (filter #(% :done?) q))]
          [:div.row
           [:div.col-sm-4
            (if @(rf/subscribe [::subs/uploading?])
              (str "Uploading " done "/" total "...")
              "Upload complete!")]
           [:progress.col-sm-8 {:max   (if (= total 0) 1 total)
                                :value (if (= total 0) 1 done)}]])]]]
     [:div.col-sm-2]]]])
