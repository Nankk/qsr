(ns qsr.const
  (:require [qsr.config :as config]))

(def tips ["アイテムは手動で並び替えることができます。モバイル端末から操作する場合は、タップしたまま短くホールドしてから動かします。"
           "Uploaderは現在モバイル端末から使用できないようです。（原因解明中）"
           "アイテムリストに加えられた変更は即座にサーバーに反映されます。（Reload image listボタンはほとんどデバッグ用途です。）"
           ])

(defn random-tip []
  (let [idx (rand-int (count tips))]
    (tips idx)))

(def server-url
  (if config/debug?
    "http://localhost:55551"
    "https://nankk.net:55551"))
