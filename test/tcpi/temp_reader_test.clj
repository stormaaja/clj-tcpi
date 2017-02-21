(ns tcpi.temp-reader-test
  (:require [clojure.test :refer :all]
            [clojure.algo.generic.math-functions :as math]
            [tcpi.temp-reader :refer :all]))

(def temp-string
  "4d 01 4b 46 7f ff 0c 10 c0 : crc=c0 YES
  4d 01 4b 46 7f ff 0c 10 c0 t=20812")

(deftest temp-string-valid-test
  (testing "Validating temperature string"
    (is (tcpi.temp-reader/is-valid? temp-string))
    (not (tcpi.temp-reader/is-valid? ""))))

(deftest temp-string-parse-test
  (testing "Parsing temperature string"
    (is
      (math/approx=
        (tcpi.temp-reader/parse-double-temperature temp-string)
        20.812
        0.001))
    ))