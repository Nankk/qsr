(ns qsr.const)

(def tips ["アイテムは手動で並び替えることができます。モバイル端末から操作する場合は、タップしたまま短くホールドしてから動かします。"
           "'Save to sheets' を押すと、現在のアイテムの並びがGoogleスプレッドシートに反映されます。この操作により、次回アクセス時に現在の並び順でアイテムを取得することができます。"
           "'Reload sheet data' を押すと、シートに加えられた変更を現在の一覧に反映させることができます。（画像が同一である場合には再取得は行いません）"
           "デベロッパーコンソールを開くと役に立つのか立たないのかよく分からないメッセージが出たり出なかったりします。気まぐれです。"
           ])

(defn random-tip []
  (let [idx (rand-int (count tips))]
    (tips idx)))

