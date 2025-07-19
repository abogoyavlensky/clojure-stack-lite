(ns {{main/ns}}.server
  (:require [clojure.tools.logging :as log]
            [integrant-extras.core :as ig-extras]
            [integrant.core :as ig]
            [muuntaja.core :as muuntaja-core]
            [reitit-extras.core :as reitit-extras]
            [reitit.coercion.malli :as coercion-malli]
            [reitit.dev.pretty :as pretty]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as ring-coercion]
            [reitit.ring.middleware.multipart :as ring-multipart]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.parameters :as ring-parameters]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.anti-forgery :as anti-forgery]
            [ring.middleware.content-type :as content-type]
            [ring.middleware.cookies :as ring-cookies]
            [ring.middleware.default-charset :as default-charset]
            [ring.middleware.keyword-params :as keyword-params]
            [ring.middleware.nested-params :as nested-params]
            [ring.middleware.not-modified :as not-modified]
            [ring.middleware.session :as ring-session]
            [ring.middleware.session.cookie :as ring-session-cookie]
            [ring.middleware.ssl :as ring-ssl]
            [ring.middleware.x-headers :as x-headers]
            [{{main/ns}}.handlers :as handlers]
            [{{main/ns}}.routes :as app-routes])
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

(defn ring-handler
  "Return main application handler for server-side rendering."
  [{:keys [options]
    :as context}]
  (let [session-store (ring-session-cookie/cookie-store
                        {:key (reitit-extras/string->16-byte-array
                                (:session-secret-key options))})]
    (ring/ring-handler
      (ring/router
        app-routes/routes
        {:exception pretty/exception
         :data {:muuntaja muuntaja-core/instance
                :coercion coercion-malli/coercion
                :middleware [[x-headers/wrap-content-type-options :nosniff]
                             [x-headers/wrap-frame-options :sameorigin]
                             ring-ssl/wrap-hsts
                             reitit-extras/wrap-xss-protection
                             not-modified/wrap-not-modified
                             content-type/wrap-content-type
                             [default-charset/wrap-default-charset "utf-8"]
                             ring-cookies/wrap-cookies
                             [ring-session/wrap-session
                              {:cookie-attrs {:secure true
                                              :http-only true}
                               :flash true
                               :store session-store}]
                             ; add handler options to request
                             [reitit-extras/wrap-context context]
                             ; parse any request parameters
                             ring-parameters/parameters-middleware
                             ring-multipart/multipart-middleware
                             nested-params/wrap-nested-params
                             keyword-params/wrap-keyword-params
                             ; negotiate request and response
                             muuntaja/format-middleware
                             ; check CSRF token
                             anti-forgery/wrap-anti-forgery
                             ; handle exceptions
                             reitit-extras/exception-middleware
                             ; coerce request and response to spec
                             ring-coercion/coerce-exceptions-middleware
                             reitit-extras/non-throwing-coerce-request-middleware
                             ring-coercion/coerce-response-middleware]}})
      (ring/routes
        (reitit-extras/create-resource-handler-cached {:path "/assets/"
                                                       :cached? (:cache-assets? options)
                                                       :cache-control (:cache-control options)})
        (ring/redirect-trailing-slash-handler)
        (ring/create-default-handler {:not-found (handlers/default-handler "Page not found" 404)
                                      :method-not-allowed (handlers/default-handler "Method not allowed" 405)
                                      :not-acceptable (handlers/default-handler "Not acceptable" 406)})))))

(defmethod ig/init-key ::server
  [_ {:keys [options]
      :as context}]
  (log/info "[SERVER] Starting server...")
  (let [handler-fn #(ring-handler context)
        handler (if (:auto-reload? options)
                  (reitit-extras/wrap-reload handler-fn)
                  (handler-fn))]
    (jetty/run-jetty handler {:port (:port options)
                              :host "0.0.0.0"
                              :join? false})))

(defmethod ig/halt-key! ::server
  [_ server]
  (log/info "[SERVER] Stopping server...")
  (.stop server))
