(ns qsr.subs
  (:require
   [re-frame.core :as rf]
   ))

(rf/reg-sub ::name
  (fn [db _]
    (db :name)))

(rf/reg-sub ::img-list
  (fn [db _]
    (db :img-list)))

(rf/reg-sub ::sort-by
  (fn [db _]
    (db :sort-by)))

(rf/reg-sub ::sort-order
  (fn [db _]
    (db :sort-order)))

(rf/reg-sub ::reflecting?
  (fn [db _]
    (db :reflecting?)))

(rf/reg-sub ::menu-open?
  (fn [db [_ _]]
    (get-in db [:menu-open?])))

(rf/reg-sub ::current-page
  (fn [db [_ _]]
    (get-in db [:current-page])))

(rf/reg-sub ::uploading?
  (fn [db [_ _]]
    (get-in db [:uploading?])))

(rf/reg-sub ::upload-queue
  (fn [db [_ _]]
    (get-in db [:upload-queue])))

;;
