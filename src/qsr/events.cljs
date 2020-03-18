(ns qsr.events
  (:require
   [re-frame.core :as rf]
   [qsr.db :as db]
   [qsr.util :as util]))

(rf/reg-event-db
  ::initialize-db
  (fn [_ _]
    (println db/default-db)
    db/default-db))

(rf/reg-event-db
  ::select-item
  (fn [db [_ item]]
    (let [items (db :items)
          select-idx (first (keep-indexed #(when (= (%2 :id) (item :id)) %1) items))
          unselected-db (update db :items (fn [item]
                                            (vec (map #(assoc % :selected? false) item))))]
      (update-in unselected-db [:items select-idx :selected?] #(constantly true)))))

(rf/reg-event-db
  ::set-items
  (fn [db [_ items]]
    (assoc db :items items)))

(rf/reg-event-db
  ::set-sort-by
  (fn [db [_ by]]
    (assoc db :sort-by by)))

(rf/reg-event-db
  ::set-sort-order
  (fn [db [_ order]]
    (assoc db :sort-order order)))

(rf/reg-event-db
  ::sort-items
  (fn [db _]
    (let [sorted-items (sort-by (db :sort-by) (db :items))]
      (assoc db :items
             (case (db :sort-order)
               :ascending (vec sorted-items)
               :descending (vec (reverse sorted-items)))))))

(rf/reg-event-db
  ::on-manually-sorted
  (fn [db [_ from-to]]
    (let [from (from-to 0)
          to (from-to 1)]
      (-> db
          (update :items #(vec (util/shift-from-to % from to)))))))

(rf/reg-event-db
  ::will-reflect-slide
  (fn [db _]
    (assoc db :reflecting? true)))

(rf/reg-event-db
  ::did-reflect-slide
  (fn [db [_ res]]
    (println "Received response from php API:")
    (println res)
    (assoc db :reflecting? false)))
