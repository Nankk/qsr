(ns qsr.styles.main-container
  (:require [garden.core :as g]
            [garden.selectors :as s]))

(defn css []
  (g/css
   [:#main-container {:height   "100%"
                      :overflow "auto"}]
   ))
