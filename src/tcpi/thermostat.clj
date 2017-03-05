(ns tcpi.thermostat
  (:require [tcpi.temp-reader :as temp]
            [clj-raspberry-gpio.gpio :as gpio])
  (:gen-class))

(def heat-cycle 60)
(def max-heat-time 30.0)
(def min-heat-time 5.0)
(def max-heat-ratio 2.4)

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
  (let [{heating :heating pin :pin time :time} state
        {target :target sensor :sensor} temperature]
    (gpio/output pin (heating-state heating))
    (keep-temperature
      (merge temperature {:current (read-temperature sensor) })
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
  [sensor target pin state-changed]
  (add-shutdown-clean pin)
  (gpio/setup pin gpio/out)
  (keep-temperature
    {:current (read-temperature sensor)
      :target target
      :sensor sensor}
    {:heating false :time 1 :pin pin}
    state-changed))
