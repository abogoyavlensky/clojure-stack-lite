(ns io.github.abogoyavlensky.clojure-stack-lite
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]
            [clojure.test :refer :all]
            ; for the Specs
            [org.corfield.new]))

(deftest test-validation-lite-template
  (testing "template.edn is valid."
    (let [template (edn/read-string (slurp (io/resource "io/github/abogoyavlensky/clojure_stack_lite/template.edn")))]
      (is (s/valid? :org.corfield.new/template template)
          (s/explain-str :org.corfield.new/template template)))))
