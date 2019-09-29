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
 ::set-items
 (fn [db [_ vs]]
   (let [vs-sub (subvec vs 3)]
     (assoc db :items (into [] (map-indexed
                                (fn [i v]
                                  (let [url-raw (first v)
                                        img-id (second (re-matches #".*file/d/([^/]+).*" url-raw))]
                                    {:name "sample"
                                     :id img-id
                                     :index i
                                     :url (str "http://drive.google.com/uc?export=view&id=" img-id)}))
                                vs-sub))))))
