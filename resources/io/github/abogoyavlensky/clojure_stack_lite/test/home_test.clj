(ns {{main/ns}}.home-test
  (:require [clj-http.client :as http]
            [clojure.test :refer :all]
            [hickory.select :as select]
            [integrant-extras.tests :as ig-extras]
            [reitit-extras.tests :as reitit-extras]
            [{{main/ns}}.server :as-alias server]
            [{{main/ns}}.test-utils :as test-utils]))

(use-fixtures :once
  (ig-extras/with-system))

(use-fixtures :each
  test-utils/with-truncated-tables)

(deftest test-home-page-is-loaded-correctly
  (let [url (reitit-extras/get-server-url (test-utils/server) :host)
        body (test-utils/response->hickory (http/get url))]
    (is (= "Clojure Stack Lite"
           (->> body
                (select/select (select/tag :span))
                (first)
                :content
                (first))))))
