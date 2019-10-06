(ns qsr.events
  (:require
   [re-frame.core :as re-frame]
   [qsr.db :as db]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   (println db/default-db)
   db/default-db))

(re-frame/reg-event-db
 ::select-item
 (fn [db [_ item]]
   (let [items (db :items)
         select-idx (first (keep-indexed #(when (= (%2 :id) (item :id)) %1) items))
         unselected-db (update db :items (fn [item]
                                           (into [] (map #(assoc % :selected? false) item))))]
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
               :ascending (into [] sorted-items)
               :descending (into [] (reverse sorted-items)))))))
