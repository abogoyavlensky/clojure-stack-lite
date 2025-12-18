(ns {{main/ns}}.auth-logout-test
  (:require [clj-http.client :as http]
            [clojure.test :refer :all]
            [{{main/ns}}.auth.queries :as queries]
            [{{main/ns}}.test-utils :as utils]
            [reitit-extras.tests :as reitit-extras]))

(use-fixtures :once
  (utils/with-system))

(use-fixtures :each
  utils/with-truncated-tables)

(deftest test-post-logout
  (let [base-url (reitit-extras/get-server-url (utils/server))
        logout-url (str base-url "/auth/logout")
        user (queries/create-user! (utils/db) {:email "user@example.com"
                                               :password "password123"})
        response (http/post logout-url {:cookies (reitit-extras/session-cookies
                                                   {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN
                                                    :identity (select-keys user [:id :email])}
                                                   utils/TEST-SECRET-KEY)
                                        :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN}})
        session-cookie-value (get-in response [:cookies "ring-session" :value])]
    (testing "Logout should redirect to home page"
      (is (= 200 (:status response)))
      (is (= "/" (get (:headers response) "HX-Redirect"))))
    (testing "Session should be empty after logout"
      (is (= {} (reitit-extras/decrypt-session-from-cookie session-cookie-value utils/TEST-SECRET-KEY))))))

(deftest test-post-logout-unauthenticated
  (let [base-url (reitit-extras/get-server-url (utils/server))
        logout-url (str base-url "/auth/logout")
        response (http/post logout-url {:redirect-strategy :none
                                        :cookies (reitit-extras/session-cookies
                                                   {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN}
                                                   utils/TEST-SECRET-KEY)
                                        :headers {reitit-extras/CSRF-TOKEN-HEADER utils/TEST-CSRF-TOKEN}})
        session-cookie-value (get-in response [:cookies "ring-session" :value])]
    (testing "Logout should redirect to home page"
      (is (= 200 (:status response)))
      (is (= "/" (get (:headers response) "HX-Redirect"))))
    (testing "Session should be empty after logout"
      (is (= {} (reitit-extras/decrypt-session-from-cookie session-cookie-value utils/TEST-SECRET-KEY))))))
