(ns qsr.events
  (:require
   [re-frame.core :as re-frame]
   [qsr.db :as db]
   [qsr.util :as util]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   (println db/default-db)
   db/default-db))

(re-frame/reg-event-db
  ::initialized
  (fn [db _]
    (assoc db :initialized? true)))

(re-frame/reg-event-db
 ::select-item
 (fn [db [_ item]]
   (let [items (db :items)
         select-idx (first (keep-indexed #(when (= (%2 :id) (item :id)) %1) items))
         unselected-db (update db :items (fn [item]
                                           (vec (map #(assoc % :selected? false) item))))]
     (update-in unselected-db [:items select-idx :selected?] #(constantly true)))))

(re-frame/reg-event-db
  ::set-items
  (fn [db [_ items]]
    (assoc db :items items)))

(re-frame/reg-event-db
  ::set-sort-by
  (fn [db [_ by]]
    (assoc db :sort-by by)))

(re-frame/reg-event-db
  ::set-sort-order
  (fn [db [_ order]]
    (assoc db :sort-order order)))

(re-frame/reg-event-db
  ::sort-items
  (fn [db _]
    (let [sorted-items (sort-by (db :sort-by) (db :items))]
      (assoc db :items
             (case (db :sort-order)
               :ascending (vec sorted-items)
               :descending (vec (reverse sorted-items)))))))

(re-frame/reg-event-db
  ::on-manually-sorted
  (fn [db [_ from-to]]
    (let [from (from-to 0)
          to (from-to 1)]
      (-> db
          (update :items #(vec (util/shift-from-to % from to)))))))

