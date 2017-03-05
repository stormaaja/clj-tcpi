(ns tcpi.core
  (:require [tcpi.thermostat :as thermo])
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

(defn -main
  [& args]
  (if (= (count args) 3)
    (thermo/start-keep-heat
      (first args)
      (Double/parseDouble (second args))
      (Integer/parseInt (third args))
      print-state)
    (println "Missing argumenst: sensor target pin")))
