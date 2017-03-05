(ns tcpi.core
  (:require [tcpi.thermostat :as thermo]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.logger :as logger]
            [ring.middleware.reload :as reload]
            [clojure.data.json :as json])
  (:use org.httpkit.server)
  (:gen-class))

(defn heating-to-string
  [heating]
  (if heating
    "Heating"
    "Idle"))

(defn print-state
  [temperature state]
  (printf "%f/%f %s %d          \r"
    (:current temperature)
    (:target temperature)
    (heating-to-string (:heating state))
    (:time state))
  (flush))

(defn third
  [coll]
  (nth coll 2))

(defn console-main
  [& args]
  (if (= (count args) 3)
    (thermo/start-keep-heat
      (first args)
      (Double/parseDouble (second args))
      (Integer/parseInt (third args))
      print-state)
    (println "Missing argumenst: sensor target pin")))

(defonce channels (atom #{}))

(defn broadcast [message]
  (do
    (doseq [channel @channels]
      (send! channel message))))

(defn on-channel-close
  [channel status]
  (swap! channels #(remove #{channel} %)))

(defn on-channel-open
  [channel]
  (swap! channels conj channel))

(defn on-channel-receive
  [channel data])

(defn ws-handler [req]
  (with-channel req channel
    (on-close channel (partial on-channel-close channel))
    (if (websocket? channel)
      (on-channel-open channel))
    (on-receive channel (partial on-channel-receive channel))))

(defroutes app-routes
  (route/resources "/")
  (GET "/ws" [] ws-handler)
  (route/not-found "Not Found"))

(defn app [])

(defn -main
  [& args]
  (org.apache.log4j.BasicConfigurator/configure)
  (println "Starting server")
  (run-server
    (logger/wrap-with-logger (reload/wrap-reload #'app-routes))
    {:port 8080})
  (println "Starting thermostat")
  (thermo/start-keep-heat
    "/tmp/sensor"
    30.0
    17
    (fn [temperature state]
      (broadcast (json/write-str
        { :temperature temperature
          :state state })))))
