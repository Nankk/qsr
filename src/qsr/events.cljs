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
   (assoc db :items (subvec
                     (into []
                           (for [v vs]
                             {:name "sample"
                              :url (let [url-raw (first v)
                                         img-id (second (re-matches #".*file/d/([^/]+).*" url-raw))]
                                     (str "http://drive.google.com/uc?export=view&id=" img-id))}))
                     3))))
