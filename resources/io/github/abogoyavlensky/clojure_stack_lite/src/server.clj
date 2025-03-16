(ns {{main/ns}}.server
  (:require [clojure.tools.logging :as log]
            [integrant-extras.core :as ig-extras]
            [integrant.core :as ig]
            [{{main/ns}}.handlers :as handlers]
            [{{main/ns}}.routes :as app-routes]
            [reitit-extras.core :as reitit-extras]
            [ring.adapter.jetty :as jetty])
  (:import com.zaxxer.hikari.HikariDataSource))

(defmethod ig/assert-key ::server
  [_ params]
  (ig-extras/validate-schema!
    {:component ::server
     :data params
     :schema [:map
              [:options
               [:map
                [:port pos-int?]
                [:session-secret-key string?]
                [:auto-reload? boolean?]
                [:cache-assets? {:optional true} boolean?]
                [:cache-control {:optional true} string?]]]
              [:db [:fn
                    {:error/message "Invalid datasource type"}
                    #(instance? HikariDataSource %)]]]}))

(defmethod ig/init-key ::server
  [_ {:keys [options]
      :as context}]
  (log/info (str "[SERVER] Starting server..."))
  (-> {:routes app-routes/routes
       :default-handlers {:not-found (handlers/default-handler "Page not found" 404)
                          :method-not-allowed (handlers/default-handler "Method not allowed" 405)
                          :not-acceptable (handlers/default-handler "Not acceptable" 406)}}
      (reitit-extras/get-handler-ssr context)
      (jetty/run-jetty {:port (:port options)
                        :join? false})))

(defmethod ig/halt-key! ::server
  [_ server]
  (log/info (str "[SERVER] Stopping server..."))
  (.stop server))
