(ns tcpi.core-test
  (:require [clojure.test :refer :all]
            [clojure.algo.generic.math-functions :as math]
            [clojure.java.io :as io]
            [tcpi.core :as core]))

(def temp-string
  "4d 01 4b 46 7f ff 0c 10 c0 : crc=c0 YES
  4d 01 4b 46 7f ff 0c 10 c0 t=21213")

(def test-file-path "testing_data.dat")

(defn create-demo-data-file
  [filepath]
  (spit filepath temp-string))

(deftest temp-file-read-test
  (testing "Reading temperature from file"
    (is
      (math/approx=
        (core/read-temperature test-file-path)
        21.213
        0.0001))))

(defn core-test-fixture [f]
  (create-demo-data-file test-file-path)
  (f)
  (io/delete-file test-file-path))

(use-fixtures :once core-test-fixture)