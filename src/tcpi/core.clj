(ns tcpi.core
  (:require [tcpi.temp-reader :as temp])
  (:gen-class))

(defn -main
  [& args]
  (println
    (read-temperature (first args))))

(defn read-temperature
  [filepath]
  (temp/parse-double-temperature
      (slurp filepath)))