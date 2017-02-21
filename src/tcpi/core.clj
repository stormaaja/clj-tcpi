(ns tcpi.core
  (:require [tcpi.temp-reader :as temp])
  (:gen-class))

(defn read-temperature
  [filepath]
  (def content (slurp filepath))
  (if (temp/is-valid? content)
    (temp/parse-double-temperature
      content)
    nil))

(defn -main
  [& args]
  (println
    (read-temperature (first args))))