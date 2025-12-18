(ns {{main/ns}}.auth-register-test
  (:require [buddy.hashers :as hashers]
            [clj-http.client :as http]
            [clojure.test :refer :all]
            [hickory.select :as select]
            [{{main/ns}}.db :as db]
            [{{main/ns}}.server :as-alias server]
            [{{main/ns}}.test-utils :as utils]
            [reitit-extras.tests :as reitit-extras]))

(use-fixtures :once
  (utils/with-system))

(use-fixtures :each
  utils/with-truncated-tables)

(deftest test-get-register-ok
  (let [base-url (reitit-extras/get-server-url (utils/server))
        response (http/get (str base-url "/auth/register"))
        body (utils/response->hickory response)]
    (is (= "Register"
           (->> body
                (select/select (select/tag :h2))
                (first)
                :content
                (first))))
    (is (= #{"__anti-forgery-token" "email" "password"}
           (->> body
                (select/select (select/tag :input))
                (map (comp :name :attrs))
                (set))))
    (is (= {:hx-post "/auth/register"
            :hx-target "#form-register"
            :id "form-register"}
           (dissoc (->> body (select/select (select/tag :form)) first :attrs)
                   :class :hx-swap)))))

(deftest test-post-register-ok
  (let [base-url (reitit-extras/get-server-url (utils/server))
        url (str base-url "/auth/register")
        response (http/post url {:cookies (reitit-extras/session-cookies
                                            {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN}
                                            utils/TEST-SECRET-KEY)
                                 :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                               :email "user@gmail.com"
                                               :password "secret-password"}})
        user (db/exec-one! (utils/db) {:select [:email :password]
                                       :from [:user]})]
    (is (= "user@gmail.com" (:email user)))
    (is (true? (:valid (hashers/verify "secret-password" (:password user) {:alg :bcrypt+sha512}))))
    (is (= 200 (:status response)))
    (is (= "/" (get (:headers response) "HX-Redirect")))))

(deftest test-post-register-user-already-exists
  (let [base-url (reitit-extras/get-server-url (utils/server))
        url (str base-url "/auth/register")
        test-email "existing@gmail.com"
        ; First, register a user to create the existing account
        _ (http/post url {:cookies (reitit-extras/session-cookies
                                     {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN}
                                     utils/TEST-SECRET-KEY)
                          :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                        :email test-email
                                        :password "first-password"}})
        ; Now try to register again with the same email
        response (http/post url {:cookies (reitit-extras/session-cookies
                                            {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN}
                                            utils/TEST-SECRET-KEY)
                                 :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                               :email test-email
                                               :password "second-password"}})
        body (utils/response->hickory response)
        error-messages (select/select (select/class :error-message) body)
        inputs (select/select (select/tag :input) body)]
    (is (= 1 (count error-messages)))
    (is (= 200 (:status response)))
    (is (= ["User already exists"] (-> error-messages first :content)))
    (is (= test-email (->> inputs
                           (filter #(= "email" (get-in % [:attrs :name])))
                           first
                           :attrs
                           :value)))))

(deftest test-post-register-invalid-email
  (let [base-url (reitit-extras/get-server-url (utils/server))
        url (str base-url "/auth/register")
        invalid-email "not-an-email"
        response (http/post url {:cookies (reitit-extras/session-cookies
                                            {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN}
                                            utils/TEST-SECRET-KEY)
                                 :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                               :email invalid-email
                                               :password "some-password"}})
        body (utils/response->hickory response)
        error-messages (select/select (select/class :error-message) body)
        inputs (select/select (select/tag :input) body)]
    (is (= 1 (count error-messages)))
    (is (= 200 (:status response)))
    (is (= ["Invalid email format"] (-> error-messages first :content)))
    (is (= invalid-email (->> inputs
                              (filter #(= "email" (get-in % [:attrs :name])))
                              first
                              :attrs
                              :value)))))

(deftest test-post-register-password-too-short
  (let [base-url (reitit-extras/get-server-url (utils/server))
        url (str base-url "/auth/register")
        test-email "test@example.com"
        response (http/post url {:cookies (reitit-extras/session-cookies
                                            {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN}
                                            utils/TEST-SECRET-KEY)
                                 :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                               :email test-email
                                               :password "1234567"}})
        body (utils/response->hickory response)
        error-messages (select/select (select/class :error-message) body)
        inputs (select/select (select/tag :input) body)]
    (is (= 1 (count error-messages)))
    (is (= 200 (:status response)))
    (is (= ["Should be at least 8 characters"] (-> error-messages first :content)))
    (is (= test-email (->> inputs
                           (filter #(= "email" (get-in % [:attrs :name])))
                           first
                           :attrs
                           :value)))))
