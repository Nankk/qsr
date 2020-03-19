(ns qsr.styles.global
  (:require [garden.core :as garden]
            [garden.selectors :as s]))

(defn css []
  (garden/css
   ;; Global
   [:html {:color "#575757"}]
   [:.container {:padding   "32px"
                 :max-width "initial"}]
   [:span
    [:.btn {:margin-right "1rem"}]]

   ;; Items list
   [:.card {:margin-top    "1.5rem"
            :border-radius "0.3rem !important"
            :width         "100%"
            :height        "auto"}]
   [:.card-img-top {:background          "url(\"../img/blank.gif\")"
                    :background-repeat   "no-repeat"
                    :background-position "center center"
                    :width               "100%"
                    :height              "auto"}]
   [:ul.wrap-list {:display         "flex"
                   :flex-wrap       "wrap"
                   :align-items     "stretch"
                   :list-style-type "none"
                   :margin          "0"
                   :padding         "0"}]
   [:ul.wrap-list [:li {:width  "400px"
                        :height "auto"}]]
   [:button.transparent {:background-color  "Transparent"
                         :background-repeat "no-repeat"
                         :text-align        "left"
                         :border            "none"
                         :cursor            "pointer"
                         :overflow          "visible"
                         :outline           "none"}]
   [:.hoverable:hover {:box-shadow "0 0px 20px rgba(0, 63, 255, .5)"
                       :transition "all 0.3s"}]
   [:.selected {:border           "solid 1px"
                :background-color "rgb(232, 240, 254)"
                :color            "rgb(31, 120, 227)"
                :font-weight      "bold"}]
   ;; File uploader
   [:hr {:height           "1px"
         :background-color "#A4A4A4"
         :border           "none"}]
   [:.sink-zone {;; :width  "600px"
                 ;; :height "12rem"
                 :text-align    "center"
                 :border        "2.5px dashed #CFD4DA"
                 :border-radius "0.3rem"}]
   [:.sink-zone
    [:div {:display "inline-block"}]]
   [:.hidden-input {:width    "0.1px"
                    :height   "0.1px"
                    :opacity  "0"
                    :overflow "hidden"
                    :position "absolute"
                    :z-index  "-1"}]
   [:.hidden-input+label {:border        "1px #0095F3 solid"
                          :padding       "1rem 2rem"
                          :border-radius "3rem"
                          :font-size     "1rem"
                          :font-weight   "700"
                          :color         "#0095F3"
                          :display       "inline-block"
                          :cursor        "pointer"}]
   ))
