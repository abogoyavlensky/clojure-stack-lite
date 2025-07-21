(ns {{main/ns}}.routes
  (:require [buddy.auth :as buddy-auth]
            [buddy.auth.backends :as backends]
            [buddy.auth.middleware :as auth-middleware]
            [{{main/ns}}.auth.handlers :as auth-handlers]
            [{{main/ns}}.auth.spec :as spec]
            [{{main/ns}}.handlers :as handlers]
            [reitit-extras.core :as ext]
            [ring.util.response :as response]))

(defn wrap-login-required
  "Middleware used in routes that require authentication. Buddy checks
  if request key :identity is set to truthy value by any previous middleware.
  If the request is not authenticated, then redirect to Login page."
  [handler]
  (fn [{router :reitit.core/router
        :as request}]
    (if (buddy-auth/authenticated? request)
      (handler request)
      (response/redirect (ext/get-route router ::login)))))

(defn wrap-already-logged-in
  "Middleware used in routes that require authentication. Buddy checks
  if request key :identity is set to truthy value by any previous middleware.
  If the request is not authenticated, then redirect to Login page."
  [handler]
  (fn [{router :reitit.core/router
        :as request}]
    (if (buddy-auth/authenticated? request)
      (response/redirect (ext/get-route router ::home))
      (handler request))))

(def routes
  (let [auth-backend (backends/session)]
    [["/" {:name ::home
           :middleware [[auth-middleware/wrap-authentication auth-backend]]
           :get {:handler handlers/home-handler}
           :responses {200 {:body string?}}}]
     ["/health" {:name ::health
                 :get {:handler (fn [_] (response/response "OK"))}}]
     ["/auth"
      [""
       {:middleware [[auth-middleware/wrap-authentication auth-backend]
                     wrap-already-logged-in]}
       ["/register" {:name ::register
                     :get {:handler auth-handlers/get-register}
                     :post {:handler auth-handlers/post-register
                            :parameters {:form [:map
                                                [:email spec/Email]
                                                [:password [:string {:min 8}]]]}
                            :responses {200 {:body string?}}}}]
       ["/login" {:name ::login
                  :get {:handler auth-handlers/get-login}
                  :post {:handler auth-handlers/post-login
                         :parameters {:form [:map
                                             [:email spec/Email]
                                             [:password [:string {:min 1}]]]}
                         :responses {200 {:body string?}}}}]
       ["/forgot-password" {:name ::forgot-password
                            :get {:handler auth-handlers/get-forgot-password}
                            :post {:handler auth-handlers/post-forgot-password
                                   :parameters {:form [:map
                                                       [:email spec/Email]]}
                                   :responses {200 {:body string?}}}}]
       ["/reset-password" {:name ::reset-password
                           :get {:handler auth-handlers/get-reset-password
                                 :parameters {:query [:map
                                                      [:token string?]]}}
                           :post {:handler auth-handlers/post-reset-password
                                  :parameters {:form [:map
                                                      [:password [:string {:min 8}]]
                                                      [:confirm-password [:string {:min 8}]]
                                                      [:token string?]]}
                                  :responses {200 {:body string?}}}}]]
      ["/logout" {:name ::logout
                  :middleware [[auth-middleware/wrap-authentication auth-backend]]
                  :post {:handler auth-handlers/post-logout}}]]
     ["/account"
      {:middleware [[auth-middleware/wrap-authentication auth-backend]
                    wrap-login-required]}
      ["" {:name ::account
           :get {:handler auth-handlers/get-account
                 :responses {200 {:body string?}}}}]
      ["/change-password" {:name ::change-password
                           :post {:handler auth-handlers/post-change-password
                                  :parameters {:form [:map
                                                      [:current-password [:string {:min 1}]]
                                                      [:new-password [:string {:min 8}]]
                                                      [:confirm-new-password [:string {:min 8}]]]}
                                  :responses {200 {:body string?}}}}]]]))
