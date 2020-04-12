(ns qsr.events
  (:require
   [re-frame.core :as rf]
   [qsr.db :as db]
   [qsr.util :as util]))

(rf/reg-event-db ::initialize-db
  (fn [_ _]
    (println db/default-db)
    db/default-db))

(rf/reg-event-db ::select-item
  (fn [db [_ item]]
    (let [items (db :img-list)
          select-idx (first (keep-indexed #(when (= (%2 :id) (item :id)) %1) items))
          unselected-db (update db :img-list (fn [item]
                                               (vec (map #(assoc % :selected? false) item))))]
      (update-in unselected-db [:img-list select-idx :selected?] #(constantly true)))))

(rf/reg-event-db ::update-img-list
  (fn [db [_ img-list]]
    (assoc db :img-list img-list)))

(rf/reg-event-db ::set-sort-by
  (fn [db [_ by]]
    (assoc db :sort-by by)))

(rf/reg-event-db ::set-sort-order
  (fn [db [_ order]]
    (assoc db :sort-order order)))

(rf/reg-event-db ::sort-items
  (fn [db _]
    (let [sorted-items (sort-by (db :sort-by) (db :img-list))]
      (assoc db :img-list
             (case (db :sort-order)
               :ascending (vec sorted-items)
               :descending (vec (reverse sorted-items)))))))

(rf/reg-event-db ::on-manually-sorted
  (fn [db [_ from-to]]
    (let [from (from-to 0)
          to (from-to 1)]
      (-> db
          (update :img-list #(vec (util/shift-from-to % from to)))))))

(rf/reg-event-db ::will-reflect-slide
  (fn [db _]
    (assoc db :reflecting? true)))

(rf/reg-event-db ::did-reflect-slide
  (fn [db [_ _]]
    (assoc db :reflecting? false)))

(rf/reg-event-db ::toggle-menu-open?
  (fn [db [_ _]]
    (assoc-in db [:menu-open?] (not (db :menu-open?)))))

(rf/reg-event-db ::set-current-page
  (fn [db [_ k]]
    (assoc-in db [:current-page] k)))

;; Uploading

(rf/reg-event-db ::set-uploading?
  (fn [db [_ uploading?]]
    (assoc db :uploading? uploading?)))

(rf/reg-event-db ::add-upload-item
  (fn [db [_ item]]
    (update db :upload-queue #(conj % item))))

(rf/reg-event-db ::complete-item-upload
  (fn [db [_ id]]
    (let [idx (first (keep-indexed #(when (= (%2 :id) id) %1) (db :upload-queue)))]
      (assoc-in db [:upload-queue idx :done?] true))))

(rf/reg-event-db ::clear-upload-queue
  (fn [db [_ _]]
    (assoc db :upload-queue [])))

(rf/reg-event-db ::finish-uploading
  (fn [db [_ _]]
    (assoc db :uploading? false)))

;;
