(ns qsr.components.image-selector
  (:require
   [qsr.events :as events]
   [qsr.subs :as subs]
   [re-frame.core :as rf]
   [cljs.core.async :as async :refer [>! <! chan go timeout]]
   [cljs-http.client :as http]
   [qsr.gapis :as gapis]
   [qsr.const :as const]
   ))

;; Global

(defn- refresh []
  (println "refresh")
  (go (let [json (<! (http/get (str const/server-url "/get-image-list")
                               {:with-credentials? false}))
            list            (get-in json [:body :img-list])]
        (rf/dispatch-sync [::events/update-img-list list]))))

;; Item list

(defn- on-item-click [item]
  (println "on-item-click")
  (go (let [idx (first (keep-indexed #(when (= (%2 :id) (item :id)) %1) @(rf/subscribe [::subs/img-list])))
            _   (rf/dispatch-sync [::events/will-reflect-slide])
            _   (<! (http/get (str const/server-url "/select?img_idx=" idx)
                              {:with-credentials? false}))
            _   (rf/dispatch-sync [::events/did-reflect-slide])]
        (refresh)
        (println "Slide update finished."))))

(defn- item-card [item]
  [:li
   [:button {:class "transparent" :on-click #(on-item-click item)}
    [:div {:class (str "card hoverable" (when (item :selected?) " selected"))}
     [:img {:data-sizes "auto"
            :data-src (str const/server-url "/thumb/" (item :id) "." (item :ext))
            :class "card-img-top lazyload"}]
     [:div {:class "card-body"}
      (str "name: " (item :name))]]]])

(defn- item-list []
  [:ul {:id "item-list"
        :class "wrap-list"}
   (let [img-list @(rf/subscribe [::subs/img-list])]
     (for [item img-list]
       ^{:key item} [item-card item]))])

;; Items panel

(defn- formatted-sheets-data [vs]
  (let [vs-sub (subvec vs 3)]
    (into [] (map-indexed (fn [i v]
                            (let [url-raw (first v)
                                  img-id (second (re-matches #".*file/d/([^/]+).*" url-raw))]
                              {:id img-id
                               :sheet-idx i
                               :url (str "https://drive.google.com/uc?export=view&id=" img-id)}))
                          vs-sub))))

(defn- formatted-drive-data [vs]
  (into [] (for [v vs]
             {:id   (get v "id")
              :name (get v "name")})))

(defn- items [rs]
  (let [s-vs (formatted-sheets-data (rs :sheets))
        d-vs (formatted-drive-data (rs :drive))]
    (into [] (for [s-v s-vs]
               (let [idx (first (keep-indexed (fn [i v] (when (= (v :id) (s-v :id)) i)) d-vs))]
                 (assoc s-v :name (get-in d-vs [idx :name])))))))

(defn- update-values-in-sheet []
  (go (println "Updating sheets...")
      (<! (gapis/sheets-update "1vkNkO71CfPhft-gRYFkvTwtg23-O75Dyaq0IIiF_-Dg"
                               "Default!A2"
                               [[(count items)]]))
      (println "Updated items count in sheets.")
      (<! (gapis/sheets-update "1vkNkO71CfPhft-gRYFkvTwtg23-O75Dyaq0IIiF_-Dg"
                               (str "Default!A4:A" (+ 3 (count items)))
                               (vec (for [item items]
                                      [(str "https://drive.google.com/file/d/" (item :id) "/view?usp=sharing")]))))
      (println "Updated urls in sheets.")))

(defn- dropdown-sort-by []
  [:span {:class "dropdown"}
   [:button {:class         "btn btn-light dropdown-toggle"
             :data-toggle   "dropdown"
             :aria-haspopup true
             :aria-expanded false}
    (name @(rf/subscribe [::subs/sort-by]))]
   [:div {:class           "dropdown-menu"
          :aria-labelledby "dropdown1"}
    (for [by [:sheet-idx :name]]
      [:button {:key      by
                :class    "dropdown-item"
                :on-click (fn [_]
                            (rf/dispatch-sync [::events/set-sort-by by])
                            (rf/dispatch-sync [::events/sort-items]))}
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
