(ns qsr.style.core
  (:require [qsr.style.global :as global]
            [qsr.style.navbar :as navbar]
            [qsr.style.util :as util]
            ))

(defn summarize []
  (-> ""
      (util/appendln (global/css))
      (util/appendln (navbar/css))
      ))
