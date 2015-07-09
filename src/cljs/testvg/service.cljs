(ns testvg.service
  (:require
    [cljs.core.async :refer [chan close!]]
    [goog.net.XhrIo :as xhr])
  (:require-macros
    [cljs.core.async.macros :refer [go alt!]]))

(def base-url "http://api.vigiglobe.com")

(def project-id "vgteam-TV_Shows")

(def statistics-volume-endpoint (str base-url "/api/statistics/v1/volume?project_id=" project-id))

(defn GET [url extract-data-fn]
  (let [ch (chan)]
    (xhr/send url
              (fn [event]
                (let [res-text (-> event .-target .getResponseText)
                      res (-> (.parse js/JSON res-text)
                              (js->clj :keywordize-keys true)
                              (extract-data-fn))]
                  (go (>! ch res)
                      (close! ch)))))
    ch))

(defn get-statistics-volume []
  (GET statistics-volume-endpoint (fn [response]
                                    (let [data (first (:data response))]
                                      (do
                                        (.log js/console data)
                                        data)))))

