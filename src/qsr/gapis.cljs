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

;; Authentication & api initialization
(def scopes #js ["https://www.googleapis.com/auth/drive"])
(def auth (JWT. (embedded/credentials :client_email) nil (embedded/credentials :private_key) scopes))
(def sheets (let [constructor (. google-sheets/sheets_v4 -Sheets)]
              (constructor. (js-obj "version" "v4" "auth" auth))))

; Sheets
(defn sheets-callback [err res]
  (when err (throw err))
  (let [vs (js->clj (.. res -data -values))]
    (println (str "sheets-callback received: " vs))
    (re-frame/dispatch-sync [::events/set-items vs])))
(defn get-values-from-sheet [sheet-id range]
  (let [params (clj->js {:spreadsheetId sheet-id
                         :range range})
        cbf sheets-callback]
    (. (.. sheets -spreadsheets -values) get params cbf)))

;; Drive
(def drive (let [constructor (. google-drive/drive_v3 -Drive)]
             (constructor. (js-obj "version" "v3" "auth" auth))))
(defn drive-callback [err res]
  (when err (throw err))
  (let [files (.. res -data -files)]
    (doseq [file files]
      (. js/console log file))))
(defn get-items-in-directory [dir-id page-size fields]
  (let [params (clj->js {:q (str "'" dir-id "' in parents")
                         :pageSize page-size
                         :fields fields
                         })
        cbf drive-callback
        files (. drive -files)]
    (. files list params cbf))
  0)

