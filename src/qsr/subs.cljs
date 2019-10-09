(ns qsr.subs
  (:require
   [re-frame.core :as re-frame]
   ))

(re-frame/reg-sub
  ::name
  (fn [db _]
    (db :name)))

(re-frame/reg-sub
  ::items
  (fn [db _]
    (db :items)))

(re-frame/reg-sub
  ::initialized?
  (fn [db _]
    (db :initialized?)))

(re-frame/reg-sub
  ::sort-by
  (fn [db _]
    (db :sort-by)))

(re-frame/reg-sub
  ::sort-order
  (fn [db _]
    (db :sort-order)))
