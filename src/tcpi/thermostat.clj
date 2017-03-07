(ns tcpi.thermostat
  (:require [tcpi.temp-reader :as temp]
            [clj-raspberry-gpio.gpio :as gpio])
  (:gen-class))

(def heat-cycle 60)
(def max-heat-time 30.0)
(def min-heat-time 5.0)
(def max-heat-ratio 2.4)

(defonce current-target (atom 0.0))

(defn set-target!
  [target]
  (reset! current-target target))

(defn- heating-ratio
  [temperature]
  (min
    (/
      (- (:target temperature)
        (:current temperature))
      max-heat-ratio)
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
  [temperature cycle-state]
  (and
      (<
        (:current temperature)
        (:target temperature))
      (<
        cycle-state
        (* max-heat-time (heating-ratio temperature)))))

(defn- should-heat?
  [temperature state]
  (and
    (is-temp-for-heating?
      temperature
      (heating-cycle-state (:time state)))
    (> (* max-heat-time (heating-ratio temperature)) min-heat-time)))

(defn- heating-state
  [heating]
  (if heating
    gpio/high
    gpio/low))

(defn- keep-temperature
  [temperature state state-changed]
  (state-changed temperature state)
  (Thread/sleep 1000)
  (let [{:keys [heating pin time]} state
        {:keys [target sensor]} temperature]
    (gpio/output pin (heating-state heating))
    (keep-temperature
      (merge temperature
        {:current (read-temperature sensor)
          :target @current-target})
      (merge state
        {:heating (should-heat? temperature state)
          :time (+ time 1) })
      state-changed)))

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
    (keep-temperature
      {:current (read-temperature sensor)
        :target 0.0
        :sensor sensor}
      {:heating false :time 1 :pin pin}
      state-changed)))
