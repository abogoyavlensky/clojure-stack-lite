(ns {{main/ns}}.home-test
  (:require [clj-http.client :as http]
            [clojure.test :refer :all]
            [hickory.select :as select]
            [reitit-extras.tests :as reitit-extras]
            [{{main/ns}}.server :as-alias server]
            [{{main/ns}}.test-utils :as utils]))

(use-fixtures :once
  (utils/with-system))

(use-fixtures :each
  utils/with-truncated-tables)

(deftest test-home-page-is-loaded-correctly
  (let [url (reitit-extras/get-server-url (utils/server) :host)
        body (utils/response->hickory (http/get url))]
    (is (= "Clojure Stack Lite"
           (->> body
                (select/select (select/tag :span))
                (first)
                :content
                (first))))))
