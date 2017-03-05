(defproject tcpi "0.1.0-SNAPSHOT"
  :description "Temperature controller for Raspberry Pi powered by Clojure"
  :url "https://github.com/stormaaja/clj-tcpi"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :min-lein-version "2.0.0"
  :dependencies [
    [org.clojure/clojure "1.8.0"]
    [clj-raspberry-gpio "0.1.2"]
    [compojure "1.5.1"]
    [ring "1.5.1"]
    [ring.middleware.logger "0.5.0"]
    [http-kit "2.2.0"]
    [org.clojure/data.json "0.2.6"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler tcpi.core/app}
  :main ^:skip-aot tcpi.core
  :target-path "target/%s"
  :profiles {
    :uberjar {:aot :all}
    :test { :dependencies [[org.clojure/algo.generic "0.1.2"]]}
    :dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                          [ring/ring-mock "0.3.0"]]}})
