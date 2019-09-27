(ns qsr.events
  (:require
   [re-frame.core :as re-frame]
   [qsr.db :as db]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   (println db/default-db)
   db/default-db))

