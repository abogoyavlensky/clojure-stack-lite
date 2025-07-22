(ns {{main/ns}}.auth-reset-password-test
  (:require [buddy.sign.jwt :as jwt]
            [clj-http.client :as http]
            [clojure.test :refer :all]
            [hickory.select :as select]
            [integrant-extras.tests :as ig-extras]
            [{{main/ns}}.auth.queries :as queries]
            [{{main/ns}}.test-utils :as utils]
            [reitit-extras.tests :as reitit-extras])
  (:import (java.time Duration Instant)))

(use-fixtures :once
  (ig-extras/with-system))

(use-fixtures :each
  utils/with-truncated-tables)

(defn- create-test-token
  ([email user-id] (create-test-token email user-id 24))
  ([email user-id hours-valid]
   (let [now (Instant/now)
         claims {:sub user-id
                 :email email
                 :exp (.getEpochSecond (.plus now (Duration/ofHours hours-valid)))
                 :iat (.getEpochSecond now)}]
     (jwt/sign claims utils/TEST-SECRET-KEY {:alg :hs256}))))

(defn- create-expired-token [email user-id]
  (let [past-time (Instant/parse "2020-01-01T00:00:00Z")
        claims {:sub user-id
                :email email
                :exp (.getEpochSecond past-time)
                :iat (.getEpochSecond past-time)}]
    (jwt/sign claims utils/TEST-SECRET-KEY {:alg :hs256})))

(deftest test-get-reset-password-valid-token
  (let [base-url (reitit-extras/get-server-url (utils/server))
        test-email "user@example.com"
        user (queries/create-user! (utils/db) {:email test-email
                                               :password "password123"})
        token (create-test-token test-email (:id user))
        url (str base-url "/auth/reset-password?token=" token)
        response (http/get url)
        body (utils/response->hickory response)]

    (testing "Should show reset password form"
      (is (= 200 (:status response)))
      (is (= "Reset Your Password"
             (->> body
                  (select/select (select/tag :h2))
                  (first)
                  :content
                  (first)))))

    (testing "Check form has required fields"
      (let [inputs (->> body
                        (select/select (select/tag :input))
                        (map (comp :name :attrs))
                        (set))]
        (is (contains? inputs "password"))
        (is (contains? inputs "confirm-password"))
        (is (contains? inputs "token"))
        (is (contains? inputs (name reitit-extras/CSRF-TOKEN-FORM-KEY)))))

    (testing "Check email is displayed"
      (is (some? (->> body
                      (select/select (select/find-in-text #".*user@example.com.*"))
                      (first)))))))

(deftest test-get-reset-password-invalid-token
  (let [base-url (reitit-extras/get-server-url (utils/server))
        invalid-token "invalid.jwt.token"
        url (str base-url "/auth/reset-password?token=" invalid-token)
        response (http/get url {:throw-exceptions false})]

    (testing "Should show error page with 400 status"
      (is (= 400 (:status response)))
      (is (some? (->> (utils/response->hickory response)
                      (select/select (select/find-in-text #".*invalid.*"))
                      (first)))))))

(deftest test-get-reset-password-expired-token
  (let [base-url (reitit-extras/get-server-url (utils/server))
        test-email "user@example.com"
        test-password "password123"
        user (queries/create-user! (utils/db) {:email test-email
                                               :password test-password})
        expired-token (create-expired-token test-email (:id user))
        url (str base-url "/auth/reset-password?token=" expired-token)
        response (http/get url {:throw-exceptions false})]

    (is (= 400 (:status response)))
    (is (some? (->> (utils/response->hickory response)
                    (select/select (select/find-in-text #".*Invalid Reset Link.*"))
                    (first))))))

(deftest test-get-reset-password-missing-token
  (let [base-url (reitit-extras/get-server-url (utils/server))
        url (str base-url "/auth/reset-password")
        response (http/get url {:throw-exceptions false})]
    (is (= 400 (:status response)))
    (is (some? (->> (utils/response->hickory response)
                    (select/select
                      (select/find-in-text
                        #".*The password reset link you used is invalid or has expired.*"))
                    (first))))))

(deftest test-get-reset-password-already-logged-in
  (let [base-url (reitit-extras/get-server-url (utils/server))
        test-email "user@example.com"
        user (queries/create-user! (utils/db) {:email test-email
                                               :password "password123"})
        token (create-test-token test-email (:id user))
        url (str base-url "/auth/reset-password?token=" token)
        response (http/get url {:redirect-strategy :none
                                :cookies (reitit-extras/session-cookies
                                           {:identity (select-keys user [:id :email])}
                                           utils/TEST-SECRET-KEY)})]
    (is (= 302 (:status response)))
    (is (= "/" (get-in response [:headers "Location"])))))

(deftest test-post-reset-password-valid
  (let [base-url (reitit-extras/get-server-url (utils/server))
        url (str base-url "/auth/reset-password")
        test-email "user@example.com"
        new-password "new-secure-password"
        user (queries/create-user! (utils/db) {:email test-email
                                               :password "original-password"})
        token (create-test-token test-email (:id user))
        response (http/post url {:cookies (reitit-extras/session-cookies
                                            {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN}
                                            utils/TEST-SECRET-KEY)
                                 :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                               :password new-password
                                               :confirm-password new-password
                                               :token token}})]
    (testing "Should show success message"
      (is (= 200 (:status response)))
      (is (some? (->> (utils/response->hickory response)
                      (select/select (select/find-in-text #".*Password Reset Successful.*"))
                      (first)))))

    (testing "Verify password was actually changed in database"
      (let [updated-user (queries/get-user (utils/db) test-email)]
        (is (not= (:password user) (:password updated-user)))))))

(deftest test-post-reset-password-invalid-token
  (let [base-url (reitit-extras/get-server-url (utils/server))
        url (str base-url "/auth/reset-password")
        new-password "new-secure-password"
        response (http/post url {:throw-exceptions false
                                 :cookies (reitit-extras/session-cookies
                                            {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN}
                                            utils/TEST-SECRET-KEY)
                                 :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                               :password new-password
                                               :confirm-password new-password
                                               :token "invalid-jwt-token"}})]
    (is (= 200 (:status response)))
    (is (some? (->> (utils/response->hickory response)
                    (select/select (select/find-in-text #".*Invalid or expired token.*"))
                    (first))))))

(deftest test-post-reset-password-expired-token
  (let [base-url (reitit-extras/get-server-url (utils/server))
        url (str base-url "/auth/reset-password")
        test-email "user@example.com"
        original-password "original-password"
        new-password "new-secure-password"
        user (queries/create-user! (utils/db) {:email test-email
                                               :password original-password})
        expired-token (create-expired-token test-email (:id user))
        response (http/post url {:cookies (reitit-extras/session-cookies
                                            {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN}
                                            utils/TEST-SECRET-KEY)
                                 :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                               :password new-password
                                               :confirm-password new-password
                                               :token expired-token}})]
    (is (= 200 (:status response)))
    (is (some? (->> (utils/response->hickory response)
                    (select/select (select/find-in-text #".*Invalid or expired token.*"))
                    (first))))))

(deftest test-post-reset-password-mismatch
  (let [base-url (reitit-extras/get-server-url (utils/server))
        url (str base-url "/auth/reset-password")
        test-email "user@example.com"
        user (queries/create-user! (utils/db) {:email test-email
                                               :password "original-password"})
        token (create-test-token test-email (:id user))
        response (http/post url {:cookies (reitit-extras/session-cookies
                                            {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN}
                                            utils/TEST-SECRET-KEY)
                                 :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                               :password "new-secure-password"
                                               :confirm-password "different-password"
                                               :token token}})
        body (utils/response->hickory response)
        error-messages (select/select (select/class :error-message) body)]
    (is (= 200 (:status response)))
    (is (= 1 (count error-messages)))
    (is (= ["Passwords do not match"] (-> error-messages first :content)))))

(deftest test-post-reset-password-too-short
  (let [base-url (reitit-extras/get-server-url (utils/server))
        url (str base-url "/auth/reset-password")
        test-email "user@example.com"
        short-password "123"
        user (queries/create-user! (utils/db) {:email test-email
                                               :password "original-password"})
        token (create-test-token test-email (:id user))
        response (http/post url {:cookies (reitit-extras/session-cookies
                                            {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN}
                                            utils/TEST-SECRET-KEY)
                                 :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                               :password short-password
                                               :confirm-password short-password
                                               :token token}})
        body (utils/response->hickory response)
        error-messages (select/select (select/class :error-message) body)]
    (is (= 200 (:status response)))
    (is (pos? (count error-messages)))))

(deftest test-post-reset-password-missing-fields
  (let [base-url (reitit-extras/get-server-url (utils/server))
        url (str base-url "/auth/reset-password")
        test-email "user@example.com"
        user (queries/create-user! (utils/db) {:email test-email
                                               :password "original-password"})
        token (create-test-token test-email (:id user))
        response (http/post url {:cookies (reitit-extras/session-cookies
                                            {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN}
                                            utils/TEST-SECRET-KEY)
                                 :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                               :confirm-password "some-password"
                                               :token token}})
        body (utils/response->hickory response)
        error-messages (select/select (select/class :error-message) body)]
    (is (= 200 (:status response)))
    (is (pos? (count error-messages)))))
