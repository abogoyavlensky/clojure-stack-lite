(ns {{main/ns}}.auth.queries
  (:require [buddy.hashers :as hashers]
            [{{main/ns}}.db :as db]))

(defn create-user!
  [db {:keys [email password]}]
  (db/exec-one! db {:insert-into :user
                    :values [{:email email
                              :password (hashers/derive password {:alg :bcrypt+sha512})}]
                    :returning [:*]}))

(defn get-user
  [db email]
  (db/exec-one! db {:select [:*]
                    :from [:user]
                    :where [:= :email email]}))

(defn update-password!
  [db {:keys [id password-hash]}]
  (db/exec-one! db {:update :user
                    :set {:password password-hash}
                    :where [:= :id id]
                    :returning [:*]}))
