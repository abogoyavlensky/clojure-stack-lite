(ns {{main/ns}}.test-utils
  (:require [clj-http.client :as http]
            [clj-http.cookies :as cookies]
            [hickory.core :as hickory]
            [hickory.select :as select]
            [integrant-extras.tests :as ig-extras]
            [{{main/ns}}.db :as db]))

(def ^:const CSRF-TOKEN-KEY :__anti-forgery-token)
(def ^:const CSRF-TOKEN-HEADER "x-csrf-token")

{{test-utils-db-setup}}

(defn get-csrf-token-and-cookies
  "Return CSRF token and cookies for given page to be used in POST request."
  [page-with-form-url]
  (let [cookie-store (cookies/cookie-store)
        response (http/get page-with-form-url {:cookie-store cookie-store})
        csrf-token (->> response
                        :body
                        (hickory/parse)
                        (hickory/as-hickory)
                        (select/select (select/id CSRF-TOKEN-KEY))
                        (first)
                        :attrs
                        :value)]
    {:csrf-token csrf-token
     :cookies (cookies/get-cookies cookie-store)}))
