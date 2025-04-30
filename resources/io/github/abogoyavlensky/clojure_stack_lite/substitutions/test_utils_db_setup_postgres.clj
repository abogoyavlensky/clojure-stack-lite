(defn with-truncated-tables
  "Remove all data from all tables."
  [f]
  (let [db (::db/db ig-extras/*test-system*)]
    (doseq [table (->> {:select [:tablename]
                        :from [:pg_tables]
                        :where [:= :schemaname "public"]}
                       (db/exec! db)
                       (mapv #(-> % :tablename keyword)))
            :when (not= :ragtime_migrations table)]
      (db/exec! db {:truncate table}))
    (f)))