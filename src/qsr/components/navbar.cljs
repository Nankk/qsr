(ns qsr.components.navbar
  (:require
   [re-frame.events :as events]
   [re-frame.subs :as subs]
   ))

(defn- navbar []
  [:div
   [:nav.nav {:role "navigation"}
    [:ul.nav__list
     [:li [:a {:href "#"}
           [:i.fas.fa-image] "　Image selector"]]
     [:li [:a {:href "#"}
           [:i.fas.fa-upload] "　Upload"]]
     ]]])
