(ns {{main/ns}}.routes
  (:require [{{main/ns}}.handlers :as handlers]))

(def routes
  [["/" {:name ::index-page
         :get {:handler handlers/home-handler}
         :responses {200 {:body string?}}}]])
