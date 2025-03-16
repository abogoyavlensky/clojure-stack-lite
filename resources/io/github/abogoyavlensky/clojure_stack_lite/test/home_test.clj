(ns {{main/ns}}.home-test
  (:require [clojure.test :refer :all]
            [etaoin.api :as etaoin]
            [integrant-extras.tests :as ig-extras]
            [{{main/ns}}.server :as-alias server]
            [{{main/ns}}.test-utils :as test-utils]
            [{{main/ns}}.webdriver :as-alias webdriver]
            [reitit-extras.tests :as reitit-extras]))

(use-fixtures :once
  (ig-extras/with-system "config.e2e.edn"))

(use-fixtures :each
  test-utils/with-truncated-tables)

(deftest test-index-page-loads-correctly
  (testing "Index page loads and displays correctly"
    (let [driver (get-in ig-extras/*test-system* [::webdriver/webdriver :driver])
          server (::server/server ig-extras/*test-system*)
          url (reitit-extras/get-server-url server :container)]

      ; Navigate to home page
      (etaoin/go driver (str url "/index"))
      (etaoin/wait-visible driver {:tag :span
                                   :fn/has-text "Clojure Stack Lite"}
                           {:timeout 5})

      ; Verify page elements
      (is (etaoin/visible? driver {:tag :span
                                   :fn/has-text "Clojure Stack Lite"}))
      (is (etaoin/visible? driver {:tag :a
                                   :fn/has-text "Get Started"})))))
