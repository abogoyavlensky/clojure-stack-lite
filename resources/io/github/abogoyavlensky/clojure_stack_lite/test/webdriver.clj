(ns {{main/ns}}.webdriver
  (:require [clojure.tools.logging :as log]
            [etaoin.api :as etaoin]
            [integrant.core :as ig])
  (:import [org.testcontainers Testcontainers]
           [org.testcontainers.containers GenericContainer]))

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
        driver (etaoin/chrome-headless {:port (.getMappedPort container 4444)
                                        :host (.getHost container)
                                        :args ["--no-sandbox"]})]
    {:container container
     :driver driver}))

(defmethod ig/halt-key! ::webdriver
  [_ {:keys [driver]}]
  (log/info (str "[DB] Closing webdriver..."))
  ; Do not stop the container to be able to reuse it
  (etaoin/quit driver))
