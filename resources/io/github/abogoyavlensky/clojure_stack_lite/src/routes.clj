(ns {{main/ns}}.routes
  (:require [ring.util.response :as response]
            [{{main/ns}}.handlers :as handlers]))

(def routes
  [["/" {:name ::home
         :get {:handler handlers/home-handler}
         :responses {200 {:body string?}}}]
   ["/health" {:name ::health-check
               :get {:handler (fn [_] (response/response "OK"))}}]])
