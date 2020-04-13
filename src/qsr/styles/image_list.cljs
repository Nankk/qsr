(ns qsr.styles.image-list
  (:require [garden.core :as garden]
            [garden.selectors :as s]))

(defn css []
  (garden/css
   ;; Items list
   [:#item-list
    [:.card {:margin-top    "1.5rem"
             :border-radius "0.3rem !important"
             :width         "100%"
             :height        "auto"}]
    [:.card-img-top {:background          "#dddddd"
                     :background-repeat   "no-repeat"
                     :background-position "center center"
                     :object-fit          "contain"
                     :width               "100%"
                     :height              "100px"}]
    [:.card-body {:padding   "6px 15px"
                  :height    "60px"
                  :font-size ".9rem"
                  :width     "200px"
                  }]
    [:.ellipsis {
                 :text-overflow "ellipsis"
                 :overflow      "hidden"
                 :white-space   "nowrap"
                 }]
    [:ul.wrap-ul {:display         "flex"
                  :flex-wrap       "wrap"
                  :justify-content "center"
                  :align-items     "stretch"
                  :list-style-type "none"
                  :margin          "0"
                  :padding         "0"}]
    [:ul.wrap-ul [:li {:margin-left "1rem"
                       :display     "flex"
                       :width       "200px"
                       :height      "100%"}]]
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
    ]))
