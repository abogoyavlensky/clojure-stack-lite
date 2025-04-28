(ns io.github.abogoyavlensky.clojure-stack-lite
  (:require [clojure.java.io :as io]))

(def SUBSTITUTIONS-BASE-DIR
  "io/github/abogoyavlensky/clojure_stack_lite/substitutions/")

(def SUBSTITUTIONS-MAPPING
  {:daisyui {:fetch-assets-urls "bb_edn_daisyui.edn"}
   :sqlite {:clj-repl-cmd "bb_edn_clj_repl_cmd_sqlite.edn"
            :db-config "resources_config_edn_sqlite.edn"
            :deploy-config-kamal "kamal-deploy-config-sqlite.txt"
            :db-driver-deps "deps_edn_db_driver_deps_sqlite.edn"
            :test-utils-db-setup "test_utils_db_setup_sqlite.clj"}
   :postgres {:clj-repl-cmd "bb_edn_clj_repl_cmd_postgres.edn"
              :db-config "resources_config_edn_postgres.edn"
              :sql-result-set-config "src_db_sql_result_set_config_postgres.edn"
              :ci-deploy-env-vars "github_workflows_deploy_ci_deploy_env_vars_postgres.txt"
              :deploy-config-kamal "kamal-deploy-config-postgres.txt"
              :deploy-secrets-kamal "kamal-deploy-secrets-postgres.txt"
              :db-driver-deps "deps_edn_db_driver_deps_postgres.edn"
              :db-test-deps "deps_edn_db_test_deps_postgres.edn"
              :test-utils-db-setup "test_utils_db_setup_postgres.clj"}})

(defn- get-file-content
  [file-name]
  (slurp (io/resource (str SUBSTITUTIONS-BASE-DIR file-name))))

(defn- replace-vars
  [mapping]
  (reduce-kv
    (fn [acc k v]
      (assoc acc k (get-file-content v)))
   {}
   mapping))

(defn data-fn
  "Example data-fn handler.

  Result is merged onto existing options data.
  Returning nil means no changes to options data."
  [data]
  (let [db (:db data :sqlite)]
    (cond-> {:fetch-assets-urls ""
             :clj-repl-cmd ""
             :db-config ""
             :sql-result-set-config ""
             :ci-deploy-env-vars ""
             :deploy-config-kamal ""
             :deploy-secrets-kamal ""
             :db-driver-deps ""
             :db-test-deps ""}
      (:daisyui data) (merge (replace-vars (:daisyui SUBSTITUTIONS-MAPPING)))
      db (merge (replace-vars (get SUBSTITUTIONS-MAPPING db))))))

(defn post-process-fn
  "Example post-process-fn handler.

  Can programmatically modify files in the generated project."
  [edn data]
  (when (true? (:debug data))
    (println "post-process-fn not modifying" (:target-dir data))))

(def RENAMINGS
  {:daisyui {"resources_public_css_default" "resources_public_css_daisyui"}})

(def EXTENSIONS
  {:daisyui [["resources_public_js_daisyui" "resources/public/js"]]
   :sqlite [["db_sqlite" "db"]
            ["resources_migrations_sqlite" "resources/migrations"]]
   :postgres [["resources_migrations_postgres" "resources/migrations"]
              ["docker-compose-postgres" ""]]})

(defn- apply-transform-source-dir
  [suffix transform]
  (let [transform-renamed (reduce
                            (fn [acc v]
                              (let [origin-source-dir (first v)
                                    new-source-dir (get-in RENAMINGS [suffix origin-source-dir])]
                                (if (some? new-source-dir)
                                  (conj acc (assoc v 0 new-source-dir))
                                  (conj acc v))))
                            []
                            transform)]
    (concat transform-renamed (get EXTENSIONS suffix))))

; Transform dirs
(defn template-fn
  "Example template-fn handler.

  Result is used as the EDN for the template."
  [edn data]
  (when (true? (:debug data))
    (println "template-fn has got data:")
    (prn data)
    (println "template-fn given edn:")
    (prn edn))

  ; TODO: add validation of options!

  (let [db (:db data (:db edn))
        new-transform (cond->> (:transform edn)
                        (:daisyui data) (apply-transform-source-dir :daisyui)
                        db (apply-transform-source-dir db))
        result (assoc edn :transform new-transform)]
    (when (true? (:debug data))
      (println "template-fn returning edn:")
      (prn result))
    result))
