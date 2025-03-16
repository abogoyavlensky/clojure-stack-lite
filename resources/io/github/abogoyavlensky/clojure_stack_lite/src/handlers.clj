(ns {{main/ns}}.handlers
  (:require [{{main/ns}}.views :as views]
            [reitit-extras.core :as reitit-extras]
            [ring.util.response :as response]))

(defn home-handler
  [_]
  (reitit-extras/render-html views/home-page))

(defn default-handler
  [error-text status-code]
  (fn [_]
    (-> (views/error-page error-text)
        (reitit-extras/render-html)
        (response/status status-code))))
