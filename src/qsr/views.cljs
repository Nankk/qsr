(ns qsr.views
  (:require
   [re-frame.core :as rf]
   [reagent.core :as reagent]
   [qsr.subs :as subs]
   [qsr.events :as events]
   [qsr.db :as db]
   [clojure.string :as str]
   [qsr.gapis :as gapis]
   [cljs-http.client :as http]
   [cljs.core.async :as async :refer [>! <! chan go timeout]]
   [qsr.const :as const]
   [qsr.components.navbar :as navbar]
   [qsr.components.image-selector :as image-selector]
   [qsr.components.file-uploader :as file-uploader]
   ["react-split-pane" :as rsp :refer [Pane]]
   ))

;; Global ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def SplitPane (get (js->clj rsp) "default")) ; why are things going wrong like this

;; Main ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- main-container []
  (case @(rf/subscribe [::subs/current-page])
    :image-selector [image-selector/main]
    :uploader [file-uploader/main]
    [image-selector/main]))

(defn- toggle-menu []
  (let [open? @(rf/subscribe [::subs/menu-open?])
        el (. js/document querySelector "#app>div>div.SplitPane.vertical.disabled>div.Pane.vertical.Pane1")]
    (set! (.. el -style -width) (if open? "0px" "230px"))
    (rf/dispatch-sync [::events/toggle-menu-open?])))

(defn- hamburger-handle []
  [:div {:style {:top "12px"
                 :left "32px"
                 :position "absolute"
                 :z-index "999"
                 }}
   [:a {:href "#"
        :on-click #(toggle-menu)}
    [:i.fas.fa-bars {:aria-hidden true
                     :style {:font-size "2rem" :color "#aaa"}}]]])

(defn- loading-indicator []
  (let [r? @(rf/subscribe [::subs/reflecting?])]
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
   [hamburger-handle]
   [:> SplitPane {:split "vertical" :defaultSize 0 :allowResize false}
    [:div#navbar [navbar/navbar]]
    [:div#main-container
     [main-container]
     ;; [:div
     ;;  [drop-area]]
     [loading-indicator]]]])
