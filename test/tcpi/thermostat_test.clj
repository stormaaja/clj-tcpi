(ns tcpi.thermostat-test
  (:require [clojure.test :refer :all]
            [tcpi.thermostat :refer :all]))

(deftest should-head-test
  (testing "Test should heat"
    (is (tcpi.thermostat/should-heat?
      { :current 20.0 :target 22.0 }
      { :since-start-ms 2000 :max-heating-ms 10000 }))
    (not (tcpi.thermostat/should-heat?
      { :current 22.0 :target 22.0 }
      { :since-start-ms 2000 :max-heating-ms 10000 }))
    (not (tcpi.thermostat/should-heat?
      { :current 20.0 :target 22.0 }
      { :since-start-ms 10000 :max-heating-ms 10000 }))
    (not (tcpi.thermostat/should-heat?
      { :current 22.1 :target 22.0 }
      { :since-start-ms 10001 :max-heating-ms 10000 }))))
