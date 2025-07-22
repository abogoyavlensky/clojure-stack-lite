(ns {{main/ns}}.auth-account-test
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

(deftest test-get-account-ok
  (let [base-url (reitit-extras/get-server-url (utils/server))
        user (queries/create-user! (utils/db) {:email "user@example.com"
                                               :password "secure-password"})
        url (str base-url "/account")
        response (http/get url {:cookies (reitit-extras/session-cookies
                                           {:identity (select-keys user [:id :email])}
                                           utils/TEST-SECRET-KEY)})
        body (utils/response->hickory response)]

    (testing "Account heading"
      (is (= 200 (:status response)))
      (is (= "Account settings"
             (->> body
                  (select/select (select/tag :h2))
                  (first)
                  :content
                  (first)))))

    (testing "User email is displayed"
      (is (some? (->> body
                      (select/select (select/find-in-text #".*user@example.com.*"))
                      (first)))))))

(deftest test-get-account-unauthenticated
  (let [base-url (reitit-extras/get-server-url (utils/server))
        account-url (str base-url "/account")
        response (http/get account-url {:redirect-strategy :none})]
    (is (= 302 (:status response)))
    (is (= "/auth/login" (get-in response [:headers "Location"])))))

(deftest test-post-change-password-ok
  (let [base-url (reitit-extras/get-server-url (utils/server))
        original-password "original-password"
        new-password "new-secure-password"
        user (queries/create-user! (utils/db) {:email "password-change@example.com"
                                               :password original-password})
        change-response (http/post (str base-url "/account/change-password")
                                   {:cookies (reitit-extras/session-cookies
                                               {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN
                                                :identity (select-keys user [:id :email])}
                                               utils/TEST-SECRET-KEY)
                                    :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                                  :current-password original-password
                                                  :new-password new-password
                                                  :confirm-new-password new-password}})]
    (is (= 200 (:status change-response)))
    (is (some? (->> (utils/response->hickory change-response)
                    (select/select (select/find-in-text #".*Password Updated Successfully.*"))
                    (first))))))

(deftest test-get-account-form-structure
  (let [base-url (reitit-extras/get-server-url (utils/server))
        user (queries/create-user! (utils/db) {:email "user@example.com"
                                               :password "secure-password"})
        account-url (str base-url "/account")
        response (http/get account-url {:cookies (reitit-extras/session-cookies
                                                   {:identity (select-keys user [:id :email])}
                                                   utils/TEST-SECRET-KEY)})
        body (utils/response->hickory response)]

    (testing "Verify change password form exists"
      (is (= 200 (:status response)))
      (let [form (->> body
                      (select/select (select/tag :form))
                      (filter #(= "/account/change-password" (get-in % [:attrs :hx-post])))
                      (first))]
        (is (some? form))
        (is (= "form-change-password" (get-in form [:attrs :id])))))

    (testing "Verify form has required fields"
      (let [inputs (->> body
                        (select/select (select/tag :input))
                        (map (comp :name :attrs))
                        (set))]
        (is (contains? inputs "current-password"))
        (is (contains? inputs "new-password"))
        (is (contains? inputs "confirm-new-password"))
        (is (contains? inputs (name reitit-extras/CSRF-TOKEN-FORM-KEY)))))))

(deftest test-post-change-password-wrong-current-password
  (let [base-url (reitit-extras/get-server-url (utils/server))
        test-email "user@example.com"
        user (queries/create-user! (utils/db) {:email test-email
                                               :password "correct-password"})
        url (str base-url "/account/change-password")
        new-password "new-secure-password"
        response (http/post url
                            {:cookies (reitit-extras/session-cookies
                                        {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN
                                         :identity (select-keys user [:id :email])}
                                        utils/TEST-SECRET-KEY)
                             :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                           :current-password "wrong-password"
                                           :new-password new-password
                                           :confirm-new-password new-password}})
        error-messages (->> response
                            (utils/response->hickory)
                            (select/select (select/class :error-message)))]
    (is (= 200 (:status response)))
    (is (= 1 (count error-messages)))
    (is (= ["Current password is incorrect"] (-> error-messages first :content)))))

(deftest test-post-change-password-mismatch
  (let [base-url (reitit-extras/get-server-url (utils/server))
        test-email "user@example.com"
        current-password "current-password"
        user (queries/create-user! (utils/db) {:email test-email
                                               :password current-password})
        change-password-url (str base-url "/account/change-password")
        response (http/post change-password-url
                            {:cookies (reitit-extras/session-cookies
                                        {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN
                                         :identity (select-keys user [:id :email])}
                                        utils/TEST-SECRET-KEY)
                             :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                           :current-password current-password
                                           :new-password "new-secure-password"
                                           :confirm-new-password "different-password"}})
        error-messages (->> response
                            (utils/response->hickory)
                            (select/select (select/class :error-message)))]
    (is (= 200 (:status response)))
    (is (= 1 (count error-messages)))
    (is (= ["New passwords do not match"] (-> error-messages first :content)))))

(deftest test-post-change-password-missing-fields
  (let [base-url (reitit-extras/get-server-url (utils/server))
        current-password "current-password"
        user (queries/create-user! (utils/db) {:email "user@example.com"
                                               :password current-password})
        change-password-url (str base-url "/account/change-password")
        response (http/post change-password-url
                            {:cookies (reitit-extras/session-cookies
                                        {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN
                                         :identity (select-keys user [:id :email])}
                                        utils/TEST-SECRET-KEY)
                             :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                           :current-password current-password
                                           :confirm-new-password "some-password"}})
        error-messages (->> response
                            (utils/response->hickory)
                            (select/select (select/class :error-message)))]
    (is (= 200 (:status response)))
    (is (pos? (count error-messages)))))

(deftest test-post-change-password-too-short
  (let [base-url (reitit-extras/get-server-url (utils/server))
        current-password "current-password"
        short-password "123"
        user (queries/create-user! (utils/db) {:email "user@example.com"
                                               :password current-password})
        change-password-url (str base-url "/account/change-password")
        response (http/post change-password-url
                            {:cookies (reitit-extras/session-cookies
                                        {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN
                                         :identity (select-keys user [:id :email])}
                                        utils/TEST-SECRET-KEY)
                             :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                           :current-password current-password
                                           :new-password short-password
                                           :confirm-new-password short-password}})
        error-messages (->> response
                            (utils/response->hickory)
                            (select/select (select/class :error-message)))]
    (is (= 200 (:status response)))
    (is (pos? (count error-messages)))))

(deftest test-post-change-password-same-as-current
  (let [base-url (reitit-extras/get-server-url (utils/server))
        current-password "same-password"
        user (queries/create-user! (utils/db) {:email "user@example.com"
                                               :password current-password})
        change-password-url (str base-url "/account/change-password")
        response (http/post change-password-url
                            {:cookies (reitit-extras/session-cookies
                                        {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN
                                         :identity (select-keys user [:id :email])}
                                        utils/TEST-SECRET-KEY)
                             :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                           :current-password current-password
                                           :new-password current-password
                                           :confirm-new-password current-password}})
        error-messages (->> response
                            (utils/response->hickory)
                            (select/select (select/class :error-message)))]
    (is (= 200 (:status response)))
    (is (= 1 (count error-messages)))
    (is (= ["New password must be different from current password"] (-> error-messages first :content)))))

(deftest test-post-change-password-unauthenticated
  (let [base-url (reitit-extras/get-server-url (utils/server))
        change-password-url (str base-url "/account/change-password")
        response (http/post change-password-url
                            {:redirect-strategy :none
                             ; no auth session in cookies
                             :cookies (reitit-extras/session-cookies
                                        {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN}
                                        utils/TEST-SECRET-KEY)
                             :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                           :current-password "current"
                                           :new-password "new"
                                           :confirm-new-password "new"}})]
    (is (= 302 (:status response)))
    (is (= "/auth/login" (get-in response [:headers "Location"])))))
