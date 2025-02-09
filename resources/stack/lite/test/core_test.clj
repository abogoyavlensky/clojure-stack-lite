(ns {{main/ns}}.core-test
  {:clj-kondo/config '{:linters {:private-call {:level :off}}}}
  (:require [clojure.test :refer :all]
            [{{main/ns}}.core :as core]))

(deftest test-demo-fn
  (testing "Correct test."
    (is (= 3 (core/demo-fn 1)))))
