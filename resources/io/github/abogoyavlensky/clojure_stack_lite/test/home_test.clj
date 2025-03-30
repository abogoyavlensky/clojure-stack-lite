(ns {{main/ns}}.home-test
  (:require [clojure.test :refer :all]
            [integrant-extras.tests :as ig-extras]
            [hato.client :as hato]
            [hickory.core :as hickory]
            [hickory.select :as select]
            [reitit-extras.tests :as reitit-extras]
            [{{main/ns}}.server :as-alias server]
            [{{main/ns}}.test-utils :as test-utils]))

(use-fixtures :once
  (ig-extras/with-system))

(use-fixtures :each
  test-utils/with-truncated-tables)

(deftest test-home-page-is-loaded-correctly
  (let [server (::server/server ig-extras/*test-system*)
        url (reitit-extras/get-server-url server :host)
        body (-> (hato/get url)
                 :body
                 (hickory/parse)
                 (hickory/as-hickory))]
    (is (= "Movies Lite" (->> body
                              (select/select (select/tag :span))
                              (first)
                              :content
                              (first))))))
