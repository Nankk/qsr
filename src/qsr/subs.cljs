(ns qsr.subs
  (:require
   [re-frame.core :as re-frame]
   ))

(re-frame/reg-sub
  ::name
  (fn [db _]
    (db :name)))

(re-frame/reg-sub
  ::chan
  (fn [db _]
    (db :async-chan)))
