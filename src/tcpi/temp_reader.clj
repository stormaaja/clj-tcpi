(ns tcpi.temp-reader
  (:require [clojure.string :as str])
  (:gen-class))

(defn is-valid?
  [raw]
  (and
    (str/includes? raw "YES")
    (str/includes? raw "t=")))

(defn parse-temperature
  [raw]
  (str/trim
    (subs
      raw
      (+ (str/index-of raw "t=") 2))))

(defn parse-double-temperature
  [raw]
  (/
    (bigdec (parse-temperature raw))
    1000.0))