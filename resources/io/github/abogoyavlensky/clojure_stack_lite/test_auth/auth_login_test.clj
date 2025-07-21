(ns {{main/ns}}.auth-login-test
  (:require [clj-http.client :as http]
            [clojure.test :refer :all]
            [hickory.select :as select]
            [integrant-extras.tests :as ig-extras]
            [{{main/ns}}.auth.queries :as queries]
            [{{main/ns}}.test-utils :as utils]
            [reitit-extras.tests :as reitit-extras]))

(use-fixtures :once
  (ig-extras/with-system))

(use-fixtures :each
  utils/with-truncated-tables)

(deftest test-get-login-ok
  (let [base-url (reitit-extras/get-server-url (utils/server))
        response (http/get (str base-url "/auth/login"))
        body (utils/response->hickory response)]
    (is (= "Login"
           (->> body
                (select/select (select/tag :h2))
                (first)
                :content
                (first))))
    (is (= #{(name reitit-extras/CSRF-TOKEN-FORM-KEY) "email" "password"}
           (->> body
                (select/select (select/tag :input))
                (map (comp :name :attrs))
                (set))))
    (is (= {:hx-post "/auth/login"
            :hx-target "#form-login"
            :id "form-login"}
           (dissoc (->> body (select/select (select/tag :form)) first :attrs)
                   :class :hx-swap)))))

(deftest test-post-login-success
  (let [base-url (reitit-extras/get-server-url (utils/server))
        login-url (str base-url "/auth/login")
        test-email "user@example.com"
        test-password "password123"
        _ (queries/create-user! (utils/db) {:email test-email
                                            :password test-password})
        response (http/post login-url
                            {:cookies (reitit-extras/session-cookies
                                        {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN}
                                        utils/TEST-SECRET-KEY)
                             :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                           :email test-email
                                           :password test-password}})]
    (is (= 200 (:status response)))
    (is (= "/" (get (:headers response) "HX-Redirect")))))

(deftest test-post-login-invalid-email
  (let [base-url (reitit-extras/get-server-url (utils/server))
        url (str base-url "/auth/login")
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

(deftest test-post-login-incorrect-password
  (let [base-url (reitit-extras/get-server-url (utils/server))
        login-url (str base-url "/auth/login")
        test-email "user2@example.com"
        _ (queries/create-user! (utils/db) {:email test-email
                                            :password "password123"})
        response (http/post login-url {:cookies (reitit-extras/session-cookies
                                                  {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN}
                                                  utils/TEST-SECRET-KEY)
                                       :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                                     :email test-email
                                                     :password "wrong-password"}})
        body (utils/response->hickory response)
        error-messages (select/select (select/class :error-message) body)
        inputs (select/select (select/tag :input) body)]
    (is (= 1 (count error-messages)))
    (is (= 200 (:status response)))
    (is (= ["Invalid email or password"] (-> error-messages first :content)))
    (is (= test-email (->> inputs
                           (filter #(= "email" (get-in % [:attrs :name])))
                           first
                           :attrs
                           :value)))))

(deftest test-post-login-nonexistent-user
  (let [base-url (reitit-extras/get-server-url (utils/server))
        login-url (str base-url "/auth/login")
        nonexistent-email "nonexistent@example.com"
        response (http/post login-url {:cookies (reitit-extras/session-cookies
                                                  {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN}
                                                  utils/TEST-SECRET-KEY)
                                       :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                                     :email nonexistent-email
                                                     :password "some-password"}})
        body (utils/response->hickory response)
        error-messages (select/select (select/class :error-message) body)
        inputs (select/select (select/tag :input) body)]
    (is (= 1 (count error-messages)))
    (is (= 200 (:status response)))
    (is (= ["Invalid email or password"] (-> error-messages first :content)))
    (is (= nonexistent-email (->> inputs
                                  (filter #(= "email" (get-in % [:attrs :name])))
                                  first
                                  :attrs
                                  :value)))))

(deftest test-get-login-already-logged-in
  (let [base-url (reitit-extras/get-server-url (utils/server))
        login-url (str base-url "/auth/login")
        user (queries/create-user! (utils/db) {:email "user@example.com"
                                               :password "password123"})
        response (http/get login-url {:redirect-strategy :none
                                      :cookies (reitit-extras/session-cookies {:identity user}
                                                                              utils/TEST-SECRET-KEY)})]
    (is (= 302 (:status response)))
    (is (= "/" (get-in response [:headers "Location"])))))

(deftest test-post-login-missing-email
  (let [base-url (reitit-extras/get-server-url (utils/server))
        login-url (str base-url "/auth/login")
        response (http/post login-url {:cookies (reitit-extras/session-cookies
                                                  {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN}
                                                  utils/TEST-SECRET-KEY)
                                       :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                                     :password "some-password"}})
        error-messages (->> response
                            (utils/response->hickory)
                            (select/select (select/class :error-message)))]
    (is (= 200 (:status response)))
    (is (pos? (count error-messages)))))

(deftest test-post-login-missing-password
  (let [base-url (reitit-extras/get-server-url (utils/server))
        login-url (str base-url "/auth/login")
        response (http/post login-url {:cookies (reitit-extras/session-cookies
                                                  {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN}
                                                  utils/TEST-SECRET-KEY)
                                       :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                                     :email "test@example.com"}})
        error-messages (->> response
                            (utils/response->hickory)
                            (select/select (select/class :error-message)))]
    (is (= 200 (:status response)))
    (is (pos? (count error-messages)))))

(deftest test-post-login-empty-fields
  (let [base-url (reitit-extras/get-server-url (utils/server))
        login-url (str base-url "/auth/login")
        response (http/post login-url {:cookies (reitit-extras/session-cookies
                                                  {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN}
                                                  utils/TEST-SECRET-KEY)
                                       :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                                     :email ""
                                                     :password ""}})
        error-messages (->> response
                            (utils/response->hickory)
                            (select/select (select/class :error-message)))]
    (is (= 200 (:status response)))
    (is (pos? (count error-messages)))))
