(ns qsr.styles.navbar
  (:require
   [garden.core :as g]
   [garden.selectors :as s]
   [qsr.styles.const :as const]))

(defn css []
  (g/css
   [:#navbar {:width            "100%"
              :display          "inline-block"
              :height           "100%"
              :background-color (const/base-colors 0)
              :padding          "0"
              :top              "0px"
              :position         "sticky"
              :overflow         "auto"}
    [:div :ol :ul :label {:list-style     "none"
                          :margin         0
                          :padding        0
                          :border         0
                          :font           "inherit"
                          :vertical-align "baseline"
                          }]
    [:* {:color (const/base-colors 7)}]
    [:&::-webkit-scrollbar-thumb {:background      (const/base-colors 5)
                                  :border          "2px solid transparent"
                                  :background-clip "content-box"
                                  :border-radius   "10px"
                                  :box-shadow      "none"}]
    [:ul {:width "100%"}]
    [:a {:text-decoration "none"}]
    [:header {:margin-right "auto"
              :margin-left  "auto"
              :max-width    "22.5rem"
              :margin-top   "150px"
              :box-shadow   "0 3px 12px rgba(0, 0, 0, 0.1)"}]
    ;; Top-level items
    [:.nav
     [:a :label {:display          "block"
                 :padding          "0.6rem 0.85rem"
                 :background-color (const/base-colors 0)
                 :box-shadow       (str "inset 0 -1px" (const/base-colors 0))
                 :transition       "all .15s ease-in"
                 :cursor           "pointer"}
      [:&:focus :&:hover {:background (const/base-colors 3)}]]]
    ;; First-level items
    [:.group-list
     [:a :label {:padding-left "2rem"
                 :background   (const/base-colors 1)
                 :box-shadow   (str "inset 0 -1px" (const/base-colors 2))}
      [:&:focus :&:hover {:background (const/base-colors 3)}]]]
    ;; Second-level items
    [:.sub-group-list
     [:a :label {:padding-left "4rem"
                 :background   (const/base-colors 2)
                 :box-shadow   (str "inset 0 -1px" (const/base-colors 3))}
      [:&:focus :&:hover {:background (const/base-colors 4)}]]]
    ;; Third-level items
    [:.sub-sub-group-list
     [:a :label {:padding-left "6rem"
                 :background   (const/accent-colors 3)
                 :box-shadow   (str "inset 0 -1px" (const/base-colors 4))}
      [:&:focus :&:hover {:background (const/base-colors 5)}]]]
    ;; Hide nested lists
    [:.group-list :.sub-group-list :.sub-sub-group-list {:height     "100%"
                                                         :max-height "0"
                                                         :overflow   "hidden"
                                                         :transition "max-height .4s ease-in-out"}]
    ;; When open
    [:.nav__list
     [(s/input (s/attr= :type :checkbox))
      ;; somehow this max-height needed (if not parameters list will be truncated)
      [:&:checked+label+ul {:max-height "10000px"}]
      ]]
    ;; Rotating chevron icon
    [(s/> :label :span) {:float      "right"
                         :transition "transform 0.1s ease"}]
    [:.nav__list
     [(s/input (s/attr= :type :checkbox))
      [:&:checked+label>span {:transform "rotate(90deg)"}]]]
    ;; Some cosmetics
    [:li
     [:a
      [:span {:float "right"}]]]
    ]))
