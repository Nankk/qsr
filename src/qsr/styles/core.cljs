(ns qsr.styles.core
  (:require [qsr.styles.global :as global]
            [qsr.styles.main-container :as main-container]
            [qsr.styles.navbar :as navbar]
            [qsr.styles.util :as util]
            ))

(defn summarize []
  (-> ""
      (util/appendln (global/css))
      (util/appendln (main-container/css))
      (util/appendln (navbar/css))
      ))
