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

   [:.card {:margin-top    "1.5rem"
            :border-radius "0.3rem !important"
            :width         "100%"
            :height        "auto"}]
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
