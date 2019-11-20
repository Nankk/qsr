(ns qsr.views
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as reagent]
   [qsr.subs :as subs]
   [qsr.events :as events]
   [qsr.db :as db]
   [clojure.string :as str]
   [qsr.gapis :as gapis]
   [cljs-http.client :as http]
   [cljs.core.async :as async :refer [chan go go-loop >! <!]]
   [qsr.const :as const]))

;; File uploader ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def drop-area-id "drop-area")

 ; for now just showing dropped items to the console

(defn handle-drag-over [e]
  (. e stopPropagation)
  (. e preventDefault)
  (set! (.. e -dataTransfer -dropEffect) "copy"))

(defn upload-files [files]
  (doseq [file files]
    (gapis/upload-file "1V86RuISEWxMeg8vIuKz190oBNEJIxNq0" file #())))

(defn drop-area []
  [:div {:class "container"}
   [:div {:class "row"}
    [:div {:class "col-sm-2"}]
    [:div {:class "col-sm-8"}
     [:div {:class "card"}
      [:div {:class "card-body"}
       [:div {:class "sink-zone"
              :id drop-area-id
              :on-drop (fn [e]
                         (. e stopPropagation)
                         (. e preventDefault)
                         (let [files (.. e -dataTransfer -files)]
                           (. js/console log files)))
              :on-drag-over #(handle-drag-over %)}
        ;; Icon & caption
        [:div
         [:i {:class "fas fa-cloud-upload-alt" :aria-hidden true
              :style {:font-size "6rem"
                      :color "#0095F3"}}]]
        [:p "Drop files here," [:br] "or select files to upload"]
        ;; Hidden input element
        [:input {:class "hidden-input"
                 :id "file-selector"
                 :type "file"
                 :name "files[]"
                 :multiple "Is a dummy text OK...?"
                 :on-change (fn [e]
                              (let [files (.. e -target -files)]
                                (. js/console log files)))}]
        ;; Default style of input elements is ugly so using a label instead
        [:label {:for "file-selector"} "Browse files"]]]]
     [:div {:class "col-sm-2"}]]]])

;; Item list ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn on-item-click [item]
  (go (let [a (re-frame/dispatch-sync [::events/select-item item])
            api-url "https://vrcpanorama-get-image.herokuapp.com/index.php"
            req-url (str api-url "?type=move&page=" (item :sheet-idx))
            a (re-frame/dispatch-sync [::events/will-reflect-slide])
            res (js->clj (<! (http/get req-url)))
            a (re-frame/dispatch-sync [::events/did-reflect-slide res])]
        (println "Slide update finished."))))

(defn item-card [item]
  [:li
   [:button {:class "transparent" :on-click #(on-item-click item)}
    [:div {:class (str "card hoverable" (when (item :selected?) " selected"))}
     [:img {:data-sizes "auto"
            :data-src (item :url)
            :class "card-img-top lazyload"}]
     [:div {:class "card-body"}
      (str "sheet index: " (item :sheet-idx))
      [:br]
      (str "name: " (item :name))]]]])

(defn item-list []
  [:ul {:id "item-list"
        :class "wrap-list"}
   (let [items @(re-frame/subscribe [::subs/items])]
   ;; (let [items (conj [] (@(re-frame/subscribe [::subs/items]) 0))] ; Debug purpose only
     (for [item items]
       ^{:key item} [item-card item]))])

;; Items panel ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def gapi-ch (chan))

(defn formatted-sheets-data [vs]
  (let [vs-sub (subvec vs 3)]
    (into [] (map-indexed (fn [i v]
                            (let [url-raw (first v)
                                  img-id (second (re-matches #".*file/d/([^/]+).*" url-raw))]
                              {:id img-id
                               :sheet-idx i
                               :url (str "https://drive.google.com/uc?export=view&id=" img-id)}))
                          vs-sub))))

(defn formatted-drive-data [vs]
  (into [] (for [v vs]
             {:id   (get v "id")
              :name (get v "name")})))

(defn items [rs]
  (let [s-vs (formatted-sheets-data (rs :sheets))
        d-vs (formatted-drive-data (rs :drive))]
    (into [] (for [s-v s-vs]
               (let [idx (first (keep-indexed (fn [i v] (when (= (v :id) (s-v :id)) i)) d-vs))]
                 (assoc s-v :name (get-in d-vs [idx :name])))))))

(go-loop [rs {}]
  (when-let [r (<! gapi-ch)]
    (let [new-rs (assoc rs (r :type) (r :content))]
      (if (and (contains? new-rs :drive)
               (contains? new-rs :sheets))
        (do (println "gonna set-items!")
            (re-frame/dispatch-sync [::events/set-items (items new-rs)])
            (re-frame/dispatch-sync [::events/initialized])
            (recur {}))
        (recur new-rs)))))

(defn refresh []
  (gapis/get-values-from-sheet
   "1vkNkO71CfPhft-gRYFkvTwtg23-O75Dyaq0IIiF_-Dg"
   "Default!A:A"
   (fn [err res]
     (when err (throw err))
     (. js/console log "got values from sheet.")
     (let [vs (js->clj (.. res -data -values))]
       (go (>! gapi-ch {:type :sheets :content vs})))))
  (gapis/get-items-in-directory
   "1V86RuISEWxMeg8vIuKz190oBNEJIxNq0"
   1000
   "files(name, id)"
   (fn [err res]
     (when err (throw err))
     (. js/console log "got items in directory.")
     (let [vs (js->clj (.. res -data -files))]
       (go (>! gapi-ch {:type :drive :content vs}))))))

(defn update-values-in-sheet []
  (println "Updating sheet...")
  (let [items @(re-frame/subscribe [::subs/items])]
    (gapis/update-values-in-sheet
     "1vkNkO71CfPhft-gRYFkvTwtg23-O75Dyaq0IIiF_-Dg"
     "Default!A2"
     [[(count items)]]
     (fn [err res]
       (when err (throw err))
       (println "Slides count updated.")))
    (gapis/update-values-in-sheet
     "1vkNkO71CfPhft-gRYFkvTwtg23-O75Dyaq0IIiF_-Dg"
     (str "Default!A4:A" (+ 3 (count items)))
     (vec (for [item items]
            [(str "https://drive.google.com/file/d/" (item :id) "/view?usp=sharing")]))
     (fn [err res]
       (when err (throw err))
       (println "Slides urls updated.")))))

(defn dropdown-sort-by []
  [:span {:class "dropdown"}
   [:button {:class         "btn btn-light dropdown-toggle"
             :data-toggle   "dropdown"
             :aria-haspopup true
             :aria-expanded false}
    (name @(re-frame/subscribe [::subs/sort-by]))]
   [:div {:class           "dropdown-menu"
          :aria-labelledby "dropdown1"}
    (for [by [:sheet-idx :name]]
      [:button {:key      by
                :class    "dropdown-item"
                :on-click (fn [_]
                            (re-frame/dispatch-sync [::events/set-sort-by by])
                            (re-frame/dispatch-sync [::events/sort-items]))}
       (name by)])]])

(defn dropdown-sort-order []
  [:span {:class "dropdown"}
   [:button {:class         "btn btn-light dropdown-toggle"
             :data-toggle   "dropdown"
             :aria-haspopup true
             :aria-expanded false}
    (name @(re-frame/subscribe [::subs/sort-order]))]
   [:div {:class           "dropdown-menu"
          :aria-labelledby "dropdown2"}
    (for [order [:ascending :descending]]
      [:button {:key      order
                :class    "dropdown-item"
                :on-click (fn [_]
                            (re-frame/dispatch-sync [::events/set-sort-order order])
                            (re-frame/dispatch-sync [::events/sort-items]))}
       (name order)])]])

(defn tip-card []
  [:div {:class "card"
         :style {:overflow "hidden"
                 :background-color "#FFF9F0"}}
   [:div {:class "row no-gutters"}
    [:div {:class "col-md-1"
           :style {:background-color "#FFC938"
                   :text-align "center"}}
     [:div {:style {:color "#FFFFFF"
                    :padding-top "1rem"
                    :font-size "2rem"}}
      [:i {:class "far fa-lightbulb"
           :aria-hidden true}]]]
    [:div {:class "col-md-11"}
     [:div {:class "card-body"}
      [:h5 {:class "card-title"} "今日のTip"]
      [:p {:class "card-text"} (const/random-tip)]]]]])

(defn items-panel []
  (refresh)
  [:div {:class "container"}
   [tip-card]
   [:div {:class "card"}
    [:div {:class "card-body"}
     [:span
      [:button {:class "btn btn-primary"
                :on-click #(refresh)}
       [:i {:class "fas fa-sync-alt" :aria-hidden true}]
       "　Reload sheet data"]
      [:button {:class "btn btn-primary"
                :on-click #(update-values-in-sheet)}
       [:i {:class "fas fa-save" :aria-hidden true}]
       "　Save to sheets"]]]
    [:div {:class "card-body"}
     "Sort by　"
     [dropdown-sort-by]
     "　in　"
     [dropdown-sort-order]
     "　order"]]
   [item-list]])

;; Main ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- loading-indicator []
  (let [r? @(re-frame/subscribe [::subs/reflecting?])]
    [:div.opacity-wrapper {:style {:opacity (if r? 1 0)}}
     [:div.loading-indicator {:style {:background-color (if r? "#eeeeee" "#22BBFF")}}
      [:div.vert-wrapper
       [:div.logo
        [:div.lds-ring
         [:div]
         [:div]
         [:div]]
        (if r? "Posting..." "Done!")]]]]))

(defn main-panel []
  [:div
   [items-panel]
   ;; [:div
   ;;  [drop-area]]
   [loading-indicator]]
  )

