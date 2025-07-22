(ns {{main/ns}}.auth.handlers
  (:require [buddy.hashers :as hashers]
            [buddy.sign.jwt :as jwt]
            [clojure.string :as str]
            [{{main/ns}}.auth.queries :as queries]
            [{{main/ns}}.auth.views :as views]
            [{{main/ns}}.routes :as-alias routes]
            [reitit-extras.core :as ext]
            [ring.util.response :as response])
  (:import [java.sql SQLException]
           [java.time Instant Duration]))

(def ^:const PASSWORD-HASH-ALGORITHM :bcrypt+sha512)
(def ^:const JWT-ALGORITHM :hs256)

; Common response utilities
(defn redirect-with-session
  "Create an HTMX redirect response with session data"
  [router route-name session-data]
  (-> (ext/render-html [:div])
      (response/header "HX-Redirect" (ext/get-route router route-name))
      (assoc :session session-data)))

(defn build-reset-url
  "Build a password reset URL with token"
  [request router token]
  (str (-> request :headers (get "host"))
       (ext/get-route router ::routes/reset-password)
       "?token=" token))

; Password utilities
(defn verify-password
  "Safely verify a password, returning {:valid boolean}"
  [password user-password]
  (try
    (hashers/verify password user-password {:alg PASSWORD-HASH-ALGORITHM})
    (catch Exception _e
      {:valid false})
    (catch AssertionError _e
      {:valid false})))

; JWT utilities
(defn create-reset-token
  "Create a password reset JWT token"
  [user-id email secret-key]
  (let [now (Instant/now)
        claims {:sub user-id
                :email email
                :exp (.getEpochSecond (.plus now (Duration/ofHours 24)))
                :iat (.getEpochSecond now)}]
    (jwt/sign claims secret-key {:alg JWT-ALGORITHM})))

(defn verify-reset-token
  "Verify and decode a password reset token"
  [token secret-key]
  (try
    {:valid true
     :claims (jwt/unsign token secret-key {:alg JWT-ALGORITHM})}
    (catch Exception _e
      {:valid false})))

(defn get-register
  "Display the user registration form"
  [{router :reitit.core/router}]
  (-> {:router router}
      (views/register-page)
      (ext/render-html)))

(defn- auth-session
  [user]
  {:identity (select-keys user [:id :email])})

(defn post-register
  "Process user registration form submission"
  [{:keys [context errors parameters params]
    router :reitit.core/router}]
  (if (some? errors)
    (-> {:router router
         :values params
         :errors (:humanized errors)}
        (views/register-form)
        (ext/render-html))
    (let [{:keys [email password]} (:form parameters)
          base-data {:router router
                     :values params}]
      (try
        (let [user (queries/create-user! (:db context) {:email email
                                                        :password password})]
          (redirect-with-session router ::routes/home (auth-session user)))
        (catch SQLException e
          (let [error-msg (if (re-find #"unique constraint" (str/lower-case (ex-message e)))
                            "user already exists"
                            "unexpected database error while creating account")]
            (-> (assoc base-data :errors {:email [error-msg]})
                (views/register-form)
                (ext/render-html))))
        (catch Exception _e
          (-> (assoc base-data :errors {:common ["unexpected server error"]})
              (views/register-form)
              (ext/render-html)))))))

(defn get-login
  "Display the user login form"
  [{router :reitit.core/router}]
  (-> {:router router}
      (views/login-page)
      (ext/render-html)))

(defn post-login
  "Process user login form submission"
  [{:keys [errors params parameters context]
    router :reitit.core/router}]
  (if (some? errors)
    (-> {:router router
         :values params
         :errors (:humanized errors)}
        (views/login-form)
        (ext/render-html))
    (let [{:keys [email password]} (:form parameters)
          user (queries/get-user (:db context) email)
          {:keys [valid]} (verify-password password (:password user))]
      (if (and (some? user) valid)
        (redirect-with-session router ::routes/home (auth-session user))
        (-> {:router router
             :values params
             :errors {:common ["Invalid email or password"]}}
            (views/login-form)
            (ext/render-html))))))

(defn post-logout
  "Log out the current user and redirect to home"
  [{router :reitit.core/router}]
  (redirect-with-session router ::routes/home nil))

(defn get-account
  "Display the user account page"
  [request]
  (-> {:user (:identity request)
       :router (:reitit.core/router request)}
      (views/account-page)
      (ext/render-html)))

(defn post-change-password
  "Process password change form submission"
  [{:keys [context errors parameters params]
    user :identity
    router :reitit.core/router}]
  (if (seq errors)
    (-> {:user user
         :router router
         :values params
         :errors (:humanized errors)}
        (views/change-password-form)
        (ext/render-html))
    (let [{:keys [current-password new-password confirm-new-password]} (:form parameters)
          user (queries/get-user (:db context) (:email user))
          {:keys [valid]} (verify-password current-password (:password user))
          base-data {:user user
                     :router router
                     :values params}]
      (cond
        (not valid)
        (-> base-data
            (assoc :errors {:current-password ["Current password is incorrect"]})
            (views/change-password-form)
            (ext/render-html))

        (not= new-password confirm-new-password)
        (-> (assoc base-data :errors {:common ["New passwords do not match"]})
            (views/change-password-form)
            (ext/render-html))

        (= current-password new-password)
        (-> (assoc base-data :errors {:common ["New password must be different from current password"]})
            (views/change-password-form)
            (ext/render-html))

        :else
        (let [password-hash (hashers/derive new-password {:alg PASSWORD-HASH-ALGORITHM})]
          (queries/update-password! (:db context) {:id (:id user)
                                                   :password-hash password-hash})
          (-> (assoc base-data :password-changed? true)
              (dissoc :values)
              (views/change-password-form)
              (ext/render-html)))))))

(defn get-forgot-password
  "Display the forgot password form"
  [{router :reitit.core/router}]
  (-> {:router router}
      (views/forgot-password-page)
      (ext/render-html)))

(defn send-email!
  "Send password reset email (currently prints to console)"
  [{:keys [email reset-link]}]
  ; TODO: send email instead of printing to console
  (println (str "============================================\n"
                "Password Reset Link for: " email "\n"
                reset-link "\n"
                "============================================\n")))

(defn post-forgot-password
  "Process forgot password form submission and send reset email"
  [{:keys [errors params parameters context]
    router :reitit.core/router
    :as request}]
  (if (seq errors)
    (-> {:router router
         :values params
         :errors (:humanized errors)}
        (views/forgot-password-form)
        (ext/render-html))
    (let [{:keys [email]} (:form parameters)
          user (queries/get-user (:db context) email)]
      (when (some? user)
        (let [token (create-reset-token (:id user) email (:session-secret-key (:options context)))
              reset-link (build-reset-url request router token)]
          (send-email! {:email email
                        :reset-link reset-link})))
      (-> {:router router
           :email-sent? true}
          (views/forgot-password-form)
          (ext/render-html)))))

(defn get-reset-password
  "Display the password reset form with token validation"
  [{:keys [parameters context]
    router :reitit.core/router}]
  (let [token (get-in parameters [:query :token])
        {:keys [valid claims]} (verify-reset-token token (:session-secret-key (:options context)))]
    (if valid
      (ext/render-html (views/reset-password-page {:router router
                                                   :token token
                                                   :email (:email claims)}))
      (-> (ext/render-html (views/invalid-reset-token-page {:router router}))
          (response/status 400)))))

(defn post-reset-password
  "Process password reset form submission"
  [{:keys [errors params parameters context]
    router :reitit.core/router}]
  (let [token (:token params)
        {:keys [password confirm-password]} (:form parameters)
        {:keys [valid claims]} (verify-reset-token token (:session-secret-key (:options context)))
        base-data {:router router
                   :values params
                   :email (:email claims)
                   :token token}]
    (cond
      (not valid)
      (-> (assoc base-data :errors {:common ["Invalid or expired token"]})
          (views/reset-password-form)
          (ext/render-html))

      (seq errors)
      (-> (assoc base-data :errors (:humanized errors))
          (views/reset-password-form)
          (ext/render-html))

      (not= password confirm-password)
      (-> (assoc base-data :errors {:common ["Passwords do not match"]})
          (views/reset-password-form)
          (ext/render-html))

      :else
      (let [user-id (:sub claims)
            password-hash (hashers/derive password {:alg PASSWORD-HASH-ALGORITHM})]
        (queries/update-password! (:db context) {:id user-id
                                                 :password-hash password-hash})
        (ext/render-html (views/password-reset-success-page {:router router}))))))
