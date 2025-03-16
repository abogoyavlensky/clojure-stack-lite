(ns {{main/ns}}.test-utils
  (:require [clojure.tools.logging :as log]
            [etaoin.api :as etaoin]
            [integrant-extras.tests :as ig-extras]
            [integrant.core :as ig]
            [{{main/ns}}.db :as db])
  (:import [org.testcontainers Testcontainers]
           [org.testcontainers.containers GenericContainer]))

(defn- all-tables
  [db]
  (->> {:select [:name]
        :from [:sqlite_master]
        :where [:= :type "table"]}
       (db/exec! db)
       (map (comp keyword :name))))

(defn with-truncated-tables
  "Remove all data from all tables."
  [f]
  (let [db (::db/db ig-extras/*test-system*)]
    (doseq [table (all-tables db)
            :when (not= :schema_version table)]
      (db/exec! db {:delete-from table}))
    (f)))

(def ^:private WEBDRIVER-PORT 4444)
(def ^:private WEBDRIVER-IMAGE "selenium/standalone-chromium:131.0")

(defmethod ig/init-key ::webdriver
  [_ {:keys [server]}]
  (log/info (str "[DB] Starting webdriver..."))
  (let [server-port (.getLocalPort (first (.getConnectors server)))
        ; Expose port from local machine to container
        _ (Testcontainers/exposeHostPorts (int-array [server-port]))
        ; Start the webdriver container
        container (doto (GenericContainer. WEBDRIVER-IMAGE)
                    (.withExposedPorts (into-array Integer [(int WEBDRIVER-PORT)]))
                    (.withReuse true)
                    (.start))
        driver (etaoin/chrome-headless {:port (.getMappedPort container WEBDRIVER-PORT)
                                        :host (.getHost container)
                                        :args ["--no-sandbox"]})]
    {:container container
     :driver driver}))

(defmethod ig/halt-key! ::webdriver
  [_ {:keys [driver]}]
  (log/info (str "[DB] Closing webdriver..."))
  ; Do not stop the container to be able to reuse it
  (etaoin/quit driver))
