(ns qsr.const)

(def words ["He said to me, Why don't you NYAN? _[::::](_'w')_"
            "GO is GOD."
            "It's a beautiful day outside."
            "\"format C: /Y\""
            "Lunch is around the corner."
            "You know what?"
            "Shall we take a break?"
            "Count the toasts you've eaten in your life!"
            "Unleash the fire!"
            "You forget a thousand things every day, pal."
            "Squish that cat."])

(defn random-word []
  (let [idx (rand-int (count words))]
    (words idx)))

;; (print (random-word))
