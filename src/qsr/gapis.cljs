(ns qsr.gapis
  (:require
   [qsr.embedded :as embedded]
   ["google-auth-library" :refer (JWT GoogleAuth)]
   ["google-sheets" :as google-sheets]
   [qsr.subs :as subs]
   [qsr.events :as events]
   [re-frame.core :as re-frame]))

;; Authentication & api initialization
(def scopes #js ["https://www.googleapis.com/auth/drive"])
(def auth (JWT. (embedded/credentials :client_email) nil (embedded/credentials :private_key) scopes))
(def sheets (let [constructor (. google-sheets/sheets_v4 -Sheets)]
              (constructor. (js-obj "version" "v4" "auth" auth))))

;; Sheets
(defn sheets-callback [err res]
  (when err (throw err))
  (let [vs (js->clj (.. res -data -values))]
    (println vs)
    (re-frame/dispatch-sync [::events/set-sheets-values vs])))
(defn get-values-from-sheet [sheet-id range]
  (let [params (clj->js {:spreadsheetId sheet-id
                         :range range})
        cbf sheets-callback]
    (. (.. sheets -spreadsheets -values) get params cbf)))



