(ns qsr.const
  (:require [qsr.config :as config]))

(def tips ["アイテムは手動で並び替えることができます。モバイル端末から操作する場合は、タップしたまま短くホールドしてから動かします。"
           "デベロッパーコンソールを開くと役に立つのか立たないのかよく分からないメッセージが出たり出なかったりします。気まぐれです。"
           "Google Driveと連携していた時期は Tips が多くありましたが今や言うことが無くて困ってます…"
           "アイテムリストに加えられた変更は即座にサーバーに反映されます。（Reload image listボタンはほとんどデバッグ用途です。）"
           ])

(defn random-tip []
  (let [idx (rand-int (count tips))]
    (tips idx)))

(def server-url
  (if config/debug?
    "http://localhost:55551"
    "https://118.27.12.128:55551"))
