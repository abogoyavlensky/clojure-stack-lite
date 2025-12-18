(ns {{main/ns}}.test-utils
  (:require [clj-reload.core :as reload]
            [hickory.core :as hickory]
            [integrant.core :as ig]
            [integrant-extras.core :as ig-extras]
            [{{main/ns}}.db :as db]
            [{{main/ns}}.server :as server]))

(def ^:const TEST-CSRF-TOKEN "test-csrf-token")
(def ^:const TEST-SECRET-KEY "test-secret-key")

(def ^:dynamic *test-system* nil)

(defn with-system
  "Run the test system before tests."
  ([]
   (with-system nil))
  ([config-path]
   (fn
     [test-fn]
     (let [test-config (ig-extras/get-config :test config-path)]
       (ig/load-namespaces test-config)
       (reload/reload)
       (binding [*test-system* (ig/init test-config)]
         (try
           (test-fn)
           (finally
             (ig/halt! *test-system*))))))))

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
  (::db/db *test-system*))

(defn server
  "Get the server instance from the test system."
  []
  (::server/server *test-system*))
