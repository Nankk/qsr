(ns qsr.components.navbar
  (:require
   [qsr.events :as events]
   [qsr.subs :as subs]
   [re-frame.core :as rf]))

(defn- navbar []
  [:div
   [:nav.nav {:role "navigation"}
    [:ul.nav__list
     [:li [:a {:href "#"
               :on-click #(rf/dispatch-sync [::events/set-current-page :image-selector])}
           [:i.fas.fa-image] "　Image selector"]]
     [:li [:a {:href "#"
               :on-click #(rf/dispatch-sync [::events/set-current-page :uploader])}
           [:i.fas.fa-upload] "　Uploader"]]
     ]]])
