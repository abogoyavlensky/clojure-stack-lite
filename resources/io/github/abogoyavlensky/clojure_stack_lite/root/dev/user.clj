(ns user
  (:require [clojure.tools.namespace.repl :as repl]
            [clojure.repl.deps :as repl-deps]
            [malli.dev :as malli-dev]
            [eftest.runner :as eftest]
            [eftest.report.pretty :as eftest-report]
            [integrant.repl :as ig-repl]
            [integrant.repl.state :as state]
            [integrant-extras.core :as ig-extras]))

(repl/set-refresh-dirs "dev" "src" "test")
(malli-dev/start!)

(defn reset
  "Restart system."
  []
  (ig-repl/set-prep! #(ig-extras/read-config :dev "config.dev.edn"))
  (ig-repl/reset))

(defn stop
  "Stop system."
  []
  (ig-repl/halt))

(defn run-all-tests
  "Run all tests for the project."
  []
  (repl/refresh)
  (eftest/run-tests (eftest/find-tests "test") {:report eftest-report/report}))

(comment
  ; It's convenient to bind shortcuts to these functions in your editor.
  ; Start or restart system
  (reset)
  ; Check system state
  (keys state/system)
  ; Stop system
  (stop)
  ; Run all project tests
  (run-all-tests)

  ; Example of add-lib dynamically
  ; Sync all new libs at once
  (repl-deps/sync-deps)
  ; or sync a specific lib
  (repl-deps/add-lib 'hiccup/hiccup {:mvn/version "2.0.0-RC3"}))
