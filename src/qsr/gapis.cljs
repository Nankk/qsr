(ns qsr.gapis
  (:require
   ["google-auth-library" :refer (JWT GoogleAuth)]
   ["google-drive" :as google-drive]
   ["google-sheets" :as google-sheets]
   [re-frame.core :as rf]
   [qsr.embedded :as embedded]
   [qsr.subs :as subs]
   [qsr.events :as events]
   [cljs.core.async :as async :refer [>! go chan]]))

;; Authentication & api initialization ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def scopes #js ["https://www.googleapis.com/auth/drive"])

(def auth (JWT. (embedded/credentials :client_email) nil (embedded/credentials :private_key) scopes))

;; Sheets ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def sheets (let [constructor (. google-sheets/sheets_v4 -Sheets)]
              (constructor. (js-obj "version" "v4" "auth" auth))))

(defn sheets-get
  "Wrapper for sheets.spreadsheets.values.get().
  When no callback-fn is specified, a channel of core.async is returned"
  ([sheet-id range]
   (println "sheets-get, async edition.")
   (let [ch (chan)
         cbf (fn [err res]
               (when err (throw err))
               (go (>! ch res)))]
     (sheets-get sheet-id range cbf)
     ch))
  ([sheet-id range callback-fn]
   (println "sheets-get, normal edition.")
   (let [params (clj->js {:spreadsheetId sheet-id
                          :range range})
         cbf callback-fn]
     (println sheet-id)
     (. (.. sheets -spreadsheets -values) get params cbf))))

(defn sheets-update
  "Wrapper for sheets.spreadsheets.values.update().
  When no callback-fn is specified, a channel of core.async is returned"
  ([sheet-id range values]
   (let [ch (chan)
         cbf (fn [err res] (when err (throw err)) (go (>! ch res)))]
     (sheets-update range values cbf)
     ch))
  ([sheet-id range values callback-fn]
   (let [params (clj->js {:spreadsheetId sheet-id
                          :range range
                          :valueInputOption "RAW"
                          :resource (clj->js {:range range
                                              :values values})})
         cbf callback-fn]
     (. (.. sheets -spreadsheets -values) update params cbf))))

;; Drive ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def drive (let [constructor (. google-drive/drive_v3 -Drive)]
             (constructor. (js-obj "version" "v3" "auth" auth))))

(defn drive-view-url [content-id]
  (str "http://drive.google.com/uc?export=view&id=" content-id))

(defn drive-list
  "Wrapper for drive.files.list().
  When no callback-fn is specified, a channel of core.async is returned"
  ([dir-id page-size fields]
   (let [ch (chan)
         cbf (fn [err res] (when err (throw err)) (go (>! ch res)))]
     (drive-list dir-id page-size fields cbf)
     ch))
  ([dir-id page-size fields callback-fn]
   (let [params (clj->js {:q (str "'" dir-id "' in parents")
                          :pageSize page-size
                          :fields fields})
         cbf callback-fn
         files (. drive -files)]
     (. files list params cbf))))

(defn drive-get
  "Wrapper for drive.files.get().
  When no callback-fn is specified, a channel of core.async is returned"
  ([file-id]
   (let [ch (chan)
         cbf (fn [err res] (when err (throw err)) (go (>! ch res)))]
     (drive-get file-id cbf)
     ch))
  ([file-id callback-fn]
   (let [params (clj->js {:fileId file-id
                          :alt "media"})
         cbf callback-fn
         files (. drive -files)]
     (. files get params cbf))))

(defn drive-upload [dir-id file callback-fn]
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
