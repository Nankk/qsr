(ns qsr.gapis
  (:require
   ["google-auth-library" :refer (JWT GoogleAuth)]
   ["google-drive" :as google-drive]
   ["google-sheets" :as google-sheets]
   [re-frame.core :as re-frame]
   [qsr.embedded :as embedded]
   [qsr.subs :as subs]
   [qsr.events :as events]
   ))

;; Authentication & api initialization ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def scopes #js ["https://www.googleapis.com/auth/drive"])

(def auth (JWT. (embedded/credentials :client_email) nil (embedded/credentials :private_key) scopes))

;; Sheets ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def sheets (let [constructor (. google-sheets/sheets_v4 -Sheets)]
              (constructor. (js-obj "version" "v4" "auth" auth))))

(defn get-values-from-sheet [sheet-id range callback-fn]
  (let [params (clj->js {:spreadsheetId sheet-id
                         :range range})
        cbf callback-fn]
    (. (.. sheets -spreadsheets -values) get params cbf)))

(. js/console log (.. sheets -spreadsheets -values -update))

(defn update-values-in-sheet [sheet-id range values callback-fn]
  (let [params (clj->js {:spreadsheetId sheet-id
                         :range range
                         :valueInputOption "RAW"
                         :resource (clj->js {:range range
                                             :values values})})
        cbf callback-fn]
    (. (.. sheets -spreadsheets -values) update params cbf)))

;; Drive ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def drive (let [constructor (. google-drive/drive_v3 -Drive)]
             (constructor. (js-obj "version" "v3" "auth" auth))))

(defn get-items-in-directory [dir-id page-size fields callback-fn]
  (let [params (clj->js {:q (str "'" dir-id "' in parents")
                         :pageSize page-size
                         :fields fields})
        cbf callback-fn
        files (. drive -files)]
    (. files list params cbf)))

(defn upload-file [dir-id file callback-fn]
  (let [name "なんかする"
        file-metadata (clj->js {:name name})
        ext "抽出する"
        filebody "node関数は使えないのでBASE64がどうたらとかあれか？"
        media (clj->js {:mimeType (str "image/" ext)
                        :body filebody})
        params (clj->js {:resource file-metadata
                         :media media
                         :fields "id"})]
    "dir-idについてもなんかする（フィルタ？）"
    (if (re-matches #"(jpe?g|png|gif)" ext)
      (. (. drive -files) create params callback-fn)
      (println (str "upload-files: Unsupported format " ext)))))
