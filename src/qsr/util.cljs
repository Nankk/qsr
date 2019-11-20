(ns qsr.util)

(defn insert [v i e]
  (concat (subvec v 0 i) [e] (subvec v i)))

(defn remove-at [v i]
  (concat (subvec v 0 i) (subvec v (inc i))))

(defn shift-from-to [v from to]
  (let [from-value (v from)
        removed (vec (remove-at v from))]
    (insert removed to from-value)))
