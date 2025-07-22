(ns {{main/ns}}.handlers
  (:require [reitit-extras.core :as reitit-extras]
            [ring.util.response :as response]
            [{{main/ns}}.views :as views]))

(defn default-handler
  [error-text status-code]
  (fn [_]
    (-> (views/error-page error-text)
        (reitit-extras/render-html)
        (response/status status-code))))

(defn home-handler
  [{router :reitit.core/router
    :as request}]
  (-> {:user (:identity request)
       :router router}
      (views/home-page)
      (reitit-extras/render-html)))
