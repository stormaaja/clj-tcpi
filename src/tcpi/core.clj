(ns tcpi.core
  (:require [tcpi.thermostat :as thermo]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.logger :as logger]
            [ring.middleware.reload :as reload]
            [clojure.data.json :as json]
            [clojure.java.io :as io])
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
  [channel data]
  (let [data-json (json/read-str data :key-fn keyword)]
    (thermo/set-target (:target data-json))))

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

(defn handle-state-change
  [temperature state]
  (broadcast (json/write-str
    { :temperature temperature
      :state state })))

(defn read-config
  [config-file]
  (json/read-str
    (slurp config-file)
    :key-fn keyword))

(defn start-console-app
  [config-file]
  (let [config (read-config config-file)]
    (println "Starting thermostat")
    (thermo/start-keep-heat
      config
      print-state
      (Double/parseDouble (nth 3 args)))))

(defn start-web-app
  [config-file]
  (org.apache.log4j.BasicConfigurator/configure)
  (let [config (read-config config-file)]
    (println "Starting server")
    (run-server
      (logger/wrap-with-logger (reload/wrap-reload #'app-routes))
        {:port (:port config)})
    (println "Starting thermostat")
    (thermo/start-keep-heat config handle-state-change 0.0)))

(defn -main
  [& args]
  (let [config-file (or (first args) "config.json")]
    (if (.exists (io/as-file config-file))
      (if (= (second args) "console")
        (start-console-app config-file)
        (start-web-app config-file))
      (println "Configuration file not found"))))
