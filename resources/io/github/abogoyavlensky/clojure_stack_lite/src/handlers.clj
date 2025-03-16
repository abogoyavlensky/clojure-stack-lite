(ns tmpl-prj.handlers
  (:require [reitit-extras.core :as reitit-extras]
            [ring.util.response :as response]
            [tmpl-prj.views :as views]))

(defn home-handler
  [_]
  (reitit-extras/render-html views/home-page))

(defn default-handler
  [error-text status-code]
  (fn [_]
    (-> (views/error-page error-text)
        (reitit-extras/render-html)
        (response/status status-code))))
