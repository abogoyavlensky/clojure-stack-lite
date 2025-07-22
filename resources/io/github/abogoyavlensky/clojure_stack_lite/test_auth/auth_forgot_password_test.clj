(ns {{main/ns}}.auth-forgot-password-test
  (:require [bond.james :as bond]
            [clj-http.client :as http]
            [clojure.test :refer :all]
            [hickory.select :as select]
            [integrant-extras.tests :as ig-extras]
            [{{main/ns}}.auth.handlers :as handlers]
            [{{main/ns}}.auth.queries :as queries]
            [{{main/ns}}.test-utils :as utils]
            [reitit-extras.tests :as reitit-extras]))

(use-fixtures :once
  (ig-extras/with-system))

(use-fixtures :each
  utils/with-truncated-tables)

(deftest test-get-forgot-password-ok
  (let [base-url (reitit-extras/get-server-url (utils/server))
        response (http/get (str base-url "/auth/forgot-password"))
        body (utils/response->hickory response)]
    (testing "Check page title and form structure"
      (is (= "Forgot your password?"
             (->> body
                  (select/select (select/tag :h2))
                  (first)
                  :content
                  (first)))))

    (testing "Check form has required fields"
      (is (= #{(name reitit-extras/CSRF-TOKEN-FORM-KEY) "email"}
             (->> body
                  (select/select (select/tag :input))
                  (map (comp :name :attrs))
                  (set)))))

    (testing "Check form properties"
      (is (= {:hx-post "/auth/forgot-password"
              :hx-target "#form-forgot-password"
              :id "form-forgot-password"}
             (dissoc (->> body (select/select (select/tag :form)) first :attrs)
                     :class :hx-swap))))))

(deftest test-get-forgot-password-already-logged-in
  (let [base-url (reitit-extras/get-server-url (utils/server))
        url (str base-url "/auth/forgot-password")
        user (queries/create-user! (utils/db) {:email "user@example.com"
                                               :password "password123"})
        response (http/get url {:redirect-strategy :none
                                :cookies (reitit-extras/session-cookies
                                           {:identity (select-keys user [:id :email])}
                                           utils/TEST-SECRET-KEY)})]

    (testing "Should get a redirect to home page"
      (is (= 302 (:status response)))
      (is (= "/" (get-in response [:headers "Location"]))))))

(deftest test-post-forgot-password-existing-email
  (bond/with-spy [handlers/send-email!]
    (let [base-url (reitit-extras/get-server-url (utils/server))
          test-email "user@example.com"
          _ (queries/create-user! (utils/db) {:email test-email
                                              :password "password123"})
          url (str base-url "/auth/forgot-password")
          response (http/post url {:cookies (reitit-extras/session-cookies
                                              {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN}
                                              utils/TEST-SECRET-KEY)
                                   :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                                 :email test-email}})]

      (testing "Should show success message"
        (is (= 200 (:status response)))
        (is (some? (->> (utils/response->hickory response)
                        (select/select (select/find-in-text #"check your email.*"))
                        (first)))))

      (testing "Send email to user for existing email"
        (is (= 1 (-> handlers/send-email! bond/calls count)))))))

(deftest test-post-forgot-password-nonexistent-email
  (bond/with-spy [handlers/send-email!]
    (let [base-url (reitit-extras/get-server-url (utils/server))
          url (str base-url "/auth/forgot-password")
          response (http/post url {:cookies (reitit-extras/session-cookies
                                              {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN}
                                              utils/TEST-SECRET-KEY)
                                   :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                                 :email "nonexistent@example.com"}})]
      (testing "Should show success message even for nonexistent email"
        (is (= 200 (:status response)))
        (is (some? (->> (utils/response->hickory response)
                        (select/select (select/find-in-text #"check your email.*"))
                        (first)))))

      (testing "Do not send email if user does not exist"
        (is (= 0 (-> #'handlers/send-email! bond/calls count)))))))

(deftest test-post-forgot-password-invalid-email
  (bond/with-spy [handlers/send-email!]
    (let [base-url (reitit-extras/get-server-url (utils/server))
          url (str base-url "/auth/forgot-password")
          invalid-email "not-an-email"
          response (http/post url {:cookies (reitit-extras/session-cookies
                                              {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN}
                                              utils/TEST-SECRET-KEY)
                                   :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN
                                                 :email invalid-email}})
          body (utils/response->hickory response)
          error-messages (select/select (select/class :error-message) body)
          inputs (select/select (select/tag :input) body)]

      (testing "Should return error for invalid email format"
        (is (= 1 (count error-messages)))
        (is (= 200 (:status response)))
        (is (= ["Invalid email format"] (-> error-messages first :content)))
        (is (= invalid-email (->> inputs
                                  (filter #(= "email" (get-in % [:attrs :name])))
                                  first
                                  :attrs
                                  :value))))

      (testing "Do not send email if user does not exist"
        (is (= 0 (-> #'handlers/send-email! bond/calls count)))))))

(deftest test-post-forgot-password-missing-email
  (let [base-url (reitit-extras/get-server-url (utils/server))
        url (str base-url "/auth/forgot-password")
        response (http/post url {:cookies (reitit-extras/session-cookies
                                            {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN}
                                            utils/TEST-SECRET-KEY)
                                 :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN}})
        error-messages (->> response
                            (utils/response->hickory)
                            (select/select (select/class :error-message)))]
    (is (= 200 (:status response)))
    (is (pos? (count error-messages)))))

(deftest test-post-forgot-password-empty-email
  (let [base-url (reitit-extras/get-server-url (utils/server))
        url (str base-url "/auth/forgot-password")
        response (http/post url {:cookies (reitit-extras/session-cookies
                                            {reitit-extras/CSRF-TOKEN-SESSION-KEY utils/TEST-CSRF-TOKEN}
                                            utils/TEST-SECRET-KEY)
                                 :form-params {reitit-extras/CSRF-TOKEN-FORM-KEY utils/TEST-CSRF-TOKEN}})
        error-messages (->> response
                            (utils/response->hickory)
                            (select/select (select/class :error-message)))]
    (is (= 200 (:status response)))
    (is (pos? (count error-messages)))))
