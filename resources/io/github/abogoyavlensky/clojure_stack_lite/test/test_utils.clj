(ns {{main/ns}}.test-utils
  (:require [clj-http.client :as http]
            [clj-http.cookies :as cookies]
            [hickory.core :as hickory]
            [hickory.select :as select]
            [integrant-extras.tests :as ig-extras]
            [{{main/ns}}.db :as db]
            [{{main/ns}}.server :as server]))

(def ^:const TEST-CSRF-TOKEN "test-csrf-token")
(def ^:const TEST-SECRET-KEY "test-secret-key")

{{test-utils-db-setup}}

(defn response->hickory
  "Convert a Ring response body to a Hickory document."
  [response]
  (-> response
      :body
      (hickory/parse)
      (hickory/as-hickory)))

(defn db
  "Get the database connection from the test system."
  []
  (::db/db ig-extras/*test-system*))

(defn server
  "Get the server instance from the test system."
  []
  (::server/server ig-extras/*test-system*))
