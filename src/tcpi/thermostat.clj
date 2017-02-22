(ns tcpi.thermostat
  (:gen-class))

(defn should-heat?
  [temperature time]
  (and
    (< (:current temperature) (:target temperature))
    (< (:since-start-ms time) (:max-heating-ms time))))