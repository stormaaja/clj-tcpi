(ns tcpi.thermostat
  (:require [tcpi.temp-reader :as temp]
            [clj-raspberry-gpio.gpio :as gpio])
  (:gen-class))

(def heat-cycle 60)
(def max-heat-time 30.0)
(def min-heat-time 5.0)
(def max-heat-ratio 2.4)
(def initial-time (System/currentTimeMillis))

(defonce current-target (atom 0.0))

(defn set-target!
  [target]
  (reset! current-target target))

(defn- heating-ratio
  [temperature target]
  (min
    (/ (- target temperature) max-heat-ratio)
    1.0))

(defn read-temperature
 [filepath]
 (let [ content (slurp filepath)]
   (if (temp/is-valid? content)
     (temp/parse-double-temperature
       content)
     nil)))

(defn- heating-cycle-state
  [time]
  (mod time heat-cycle))

(defn- is-temp-for-heating?
  [temperature target cycle-state]
  (and
      (< temperature target)
      (< cycle-state (* max-heat-time (heating-ratio temperature target)))))

(defn- should-heat?
  [temperature target time-elapsed]
  (and
    (is-temp-for-heating?
      temperature
      target
      (heating-cycle-state time-elapsed))
    (> (* max-heat-time (heating-ratio temperature target)) min-heat-time)))


(defn- heating-state
  [heating]
  (if heating
    gpio/high
    gpio/low))

(defn- time-elapsed-since-start
  []
  (- (System/currentTimeMillis) initial-time))

(defn- keep-temperature
  [sensor pin target state-changed]
  (let [time-elapsed (time-elapsed-since-start)]
    (let [current-temperature (read-temperature sensor)
          heating (should-heat? current-temperature target (/ time-elapsed 1000))]
      (gpio/output
        pin
        (heating-state heating))
      (state-changed { :temperature current-temperature
        :time-elapsed time-elapsed
        :target target
        :heating heating }))
    (let [time-delta (- (time-elapsed-since-start) time-elapsed)]
      (if (< time-delta 1000)
        (Thread/sleep (- 1000 time-delta))))))

(defn- add-shutdown-clean
  [pin]
  (.addShutdownHook (Runtime/getRuntime)
    (Thread.
    (fn
      []
      (gpio/output 17 gpio/low)
      (gpio/cleanup 17)))))

(defn start-keep-heat
  [config state-changed target]
  (let [{:keys [pin sensor]} config]
    (add-shutdown-clean pin)
    (gpio/setup pin gpio/out)
    (set-target! target)
    (while true
      (keep-temperature sensor pin @current-target state-changed))))
