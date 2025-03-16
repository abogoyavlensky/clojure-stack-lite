(ns {{main/ns}}.core
  (:gen-class)
  (:require [integrant-extras.core :as ig-extras]))

(defn -main
  "Run application system in production."
  []
  (ig-extras/run-system {:profile :prod
                         :config-path "config.edn"}))
