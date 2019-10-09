(ns qsr.db)

(def default-db
  {:name "re-frame"
   :items []
   :initialized? false
   :selected-item nil
   :sort-by :sheet-idx
   :sort-order :ascending})

