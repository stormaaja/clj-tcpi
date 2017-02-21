(defproject tcpi "0.1.0-SNAPSHOT"
  :description "Temperature controller for Raspberry Pi powered by Clojure"
  :url "https://github.com/stormaaja/clj-tcpi"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [
    [org.clojure/clojure "1.8.0"]]
  :main ^:skip-aot tcpi.core
  :target-path "target/%s"
  :profiles {
    :uberjar {:aot :all}
    :test {
      :dependencies [
        [org.clojure/algo.generic "0.1.2"]]
    }
  })
