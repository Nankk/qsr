(ns qsr.db)

(def default-db
  {:items []
   :initialized? false
   :selected-item nil
   :sort-by :sheet-idx
   :sort-order :ascending
   :current-page :top
   :menu-open? false})
