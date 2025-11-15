(ns io.github.abogoyavlensky.clojure-stack-lite
  (:require [clojure.java.io :as io]))

(def SUBSTITUTIONS-BASE-DIR
  "io/github/abogoyavlensky/clojure_stack_lite/substitutions/")

(def DB-TYPES #{:sqlite :postgres})
(def DEPLOY-TYPES #{:kamal :none})

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
              :test-utils-db-setup "test_utils_db_setup_postgres.clj"}
   :auth {:auth-deps "deps_edn_auth_deps.edn"}
   :kamal {:readme-deploy-kamal "readme_deploy_kamal.md"
           :bb-deploy-kamal "bb_deploy_kamal.edn"}})

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
  (let [db (keyword (:db data :sqlite))
        deploy (keyword (:deploy data :kamal))]

    (when-not (contains? DB-TYPES db)
      (throw (Exception. "Invalid db type. Supported types are: :sqlite, :postgres")))

    (when-not (contains? DEPLOY-TYPES deploy)
      (throw (Exception. "Invalid deploy type. Supported types are: :kamal, :none")))

    (cond-> {:db db
             :deploy deploy
             :fetch-assets-urls ""
             :clj-repl-cmd ""
             :db-config ""
             :sql-result-set-config ""
             :ci-deploy-env-vars ""
             :test-utils-db-setup ""
             :deploy-config-kamal ""
             :deploy-secrets-kamal ""
             :db-driver-deps ""
             :db-test-deps ""
             :auth-deps ""
             :readme-deploy-kamal ""
             :bb-deploy-kamal ""}
      (:daisyui data) (merge (replace-vars (:daisyui SUBSTITUTIONS-MAPPING)))
      (:auth data) (merge (replace-vars (:auth SUBSTITUTIONS-MAPPING)))
      db (merge (replace-vars (get SUBSTITUTIONS-MAPPING db)))
      deploy (merge (replace-vars (get SUBSTITUTIONS-MAPPING deploy))))))

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
              ["src_postgres" "src/{{main/file}}"]
              ["test_postgres" "test"]
              ["docker-compose-postgres" ""]]
   :auth [["src_auth" "src/{{main/file}}"]
          ["test_auth" "test/{{main/file}}"]]
   :auth-sqlite [["resources_migrations_sqlite_auth" "resources/migrations"]]
   :auth-postgres [["resources_migrations_postgres_auth" "resources/migrations"]]
   :kamal [["github_workflows_deploy_yaml_kamal" ".github/workflows"]
           ["kamal" ".kamal"]]})

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

  (let [db (:db data (:db edn))
        deploy (:deploy data (:deploy edn))
        new-transform (cond->> (:transform edn)
                        (:daisyui data) (apply-transform-source-dir :daisyui)
                        (:auth data) (apply-transform-source-dir :auth)
                        (and (:auth data) (= :sqlite db)) (apply-transform-source-dir :auth-sqlite)
                        (and (:auth data) (= :postgres db)) (apply-transform-source-dir :auth-postgres)
                        db (apply-transform-source-dir db)
                        deploy (apply-transform-source-dir deploy))
        result (assoc edn :transform new-transform)]
    (when (true? (:debug data))
      (println "template-fn returning edn:")
      (prn result))
    result))
