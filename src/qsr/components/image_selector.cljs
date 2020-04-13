(ns qsr.components.image-selector
  (:require
   [qsr.events :as events]
   [qsr.subs :as subs]
   [re-frame.core :as rf]
   [cljs.core.async :as async :refer [>! <! chan go timeout]]
   [cljs-http.client :as http]
   [qsr.const :as const]
   [qsr.common :as common]))

;; Item list

(defn- on-item-click [item]
  (println "on-item-click")
  (go (let [idx (first (keep-indexed #(when (= (%2 :id) (item :id)) %1) @(rf/subscribe [::subs/img-list])))
            _   (rf/dispatch-sync [::events/will-reflect-slide])
            _   (<! (http/get (str const/server-url "/select?img_idx=" idx)
                              {:with-credentials? false}))
            _   (rf/dispatch-sync [::events/did-reflect-slide])]
        (common/refresh)
        (println "Slide update finished."))))

(defn- item-card [item]
  [:li
   [:button {:class "transparent" :on-click #(on-item-click item)}
    [:div {:class (str "card hoverable" (when (item :selected?) " selected"))}
     [:img {:data-sizes "auto"
            :data-src   (str const/server-url "/thumb/" (item :id) "." (item :ext))
            :class      "card-img-top lazyload"}]
     [:div.card-body
      [:div.ellipsis (item :name)]
      [:div {:style {:text-align "right"
                     :color      "#bbbbbb"}}
       [:i.fas.fa-times {:aria-hidden true
                         :style {:font-size "1rem"}
                         :on-click (fn [e]
                                     (. e stopPropagation)
                                     (rf/dispatch-sync [::events/delete-item (item :id)])
                                     (common/upload-image-list))}]]]]]])

(defn- item-list []
  [:ul#image-list.wrap-ul
   (let [img-list @(rf/subscribe [::subs/img-list])]
     (for [item img-list]
       ^{:key item} [item-card item]))])

;; Items panel

(defn- dropdown-sort-by []
  [:span {:class "dropdown"}
   [:button {:class         "btn btn-light dropdown-toggle"
             :data-toggle   "dropdown"
             :aria-haspopup true
             :aria-expanded false}
    (name @(rf/subscribe [::subs/sort-by]))]
   [:div {:class           "dropdown-menu"
          :aria-labelledby "dropdown1"}
    (for [by [:name]]
      [:button {:key      by
                :class    "dropdown-item"
                :on-click (fn [_]
                            (rf/dispatch-sync [::events/set-sort-by by])
                            (rf/dispatch-sync [::events/sort-items])
                            )}
       (name by)])]])

(defn- dropdown-sort-order []
  [:span {:class "dropdown"}
   [:button {:class         "btn btn-light dropdown-toggle"
             :data-toggle   "dropdown"
             :aria-haspopup true
             :aria-expanded false}
    (name @(rf/subscribe [::subs/sort-order]))]
   [:div {:class           "dropdown-menu"
          :aria-labelledby "dropdown2"}
    (for [order [:ascending :descending]]
      [:button {:key      order
                :class    "dropdown-item"
                :on-click (fn [_]
                            (rf/dispatch-sync [::events/set-sort-order order])
                            (rf/dispatch-sync [::events/sort-items]))}
       (name order)])]])

(defn- tip-card []
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

(defn main []
  (common/refresh)
  [:div
   [:div {:class "container"}
    [tip-card]
    [:div {:class "card"}
     [:div {:class "card-body"}
      [:span
       [:button {:class "btn btn-primary"
                 :on-click #(common/refresh)}
        [:i {:class "fas fa-sync-alt" :aria-hidden true}]
        "　Reload image list"]]]
     [:div {:class "card-body"}
      "Sort by　"
      [dropdown-sort-by]
      "　in　"
      [dropdown-sort-order]
      "　order"]]
    [:div#item-list [item-list]]]])
