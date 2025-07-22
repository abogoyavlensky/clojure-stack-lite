(ns {{main/ns}}.auth.views
  (:require [clojure.string :as str]
            [{{main/ns}}.routes :as-alias routes]
            [{{main/ns}}.views :as views]
            [reitit-extras.core :as ext]))

(defn- form-input
  [{:keys [input-name input-label input-type input-value errors props]}]
  [:div
   [:label {:class ["mb-2" "inline-block" "text-sm" "text-gray-800" "sm:text-base"]
            :for input-name} (str/capitalize (or input-label input-name))]
   [:input (merge {:class ["w-full" "rounded-lg" "border" "px-3" "py-2" "text-gray-800"
                           "outline-none" "ring-indigo-300" "transition" "duration-100" "focus:ring"
                           (when (seq errors) "border-red-500")]
                   :name input-name
                   :type input-type
                   :value input-value
                   :autocorrect "off"
                   :autocapitalize "none"}
                  props)]
   (for [error-message errors]
     [:p {:class ["text-red-500" "text-sm" "mt-1" "error-message"]} (str/capitalize error-message)])])

(defn register-form
  [{:keys [router errors values]}]
  [:form
   {:id "form-register"
    :class ["mx-auto" "max-w-lg"]
    :hx-post (ext/get-route router ::routes/register)
    :hx-target "#form-register"
    :hx-swap "outerHTML"}
   (ext/csrf-token-html)
   [:div {:class ["flex" "flex-col" "gap-4" "p-4" "md:p-8"]}
    (form-input {:input-name "email"
                 :input-type "email"
                 :input-value (:email values)
                 :errors (:email errors)
                 :required true
                 :props {:autocomplete "email"}})
    (form-input {:input-name "password"
                 :input-type "password"
                 :input-value (:password values)
                 :errors (:password errors)
                 :required true
                 :props {:autocomplete "new-password"}})
    [:button
     {:class ["block" "rounded-lg" "bg-gray-800" "px-8" "py-3" "text-center" "text-sm"
              "font-semibold" "text-white" "outline-none" "ring-gray-300" "transition"
              "duration-100" "hover:bg-gray-700" "focus-visible:ring" "active:bg-gray-600"
              "md:text-base" "cursor-pointer"]
      :type "submit"}
     "Create an account"]]
   [:div
    [:p {:class ["text-center" "text-sm" "text-gray-500"]}
     "Already registered? "
     [:a {:class ["text-indigo-500" "transition" "duration-100" "hover:text-indigo-600" "active:text-indigo-700"]
          :href (ext/get-route router ::routes/login)} "Log in"]]]])

(defn register-page
  [{:keys [router]
    :as args}]
  (views/base
    [:div {:class ["bg-white" "py-6" "sm:py-8" "lg:py-12" "mt-20"]}
     [:nav {:class ["absolute" "top-0" "left-1/4" "p-4"]}
      [:div {:class ["flex" "gap-4"]}
       (views/button {:url (ext/get-route router ::routes/home)
                      :text "<- Home page"})]]
     [:div {:class ["mx-auto" "max-w-screen-2xl" "px-4" "md:px-8"]}
      [:h2 {:class ["mb-4" "text-center" "text-2xl" "font-bold" "text-gray-800" "md:mb-8" "lg:text-3xl"]} "Register"]
      (register-form args)]]))

(defn- common-errors
  [errors]
  (when errors
    [:div {:class ["text-red-500" "text-sm" "mt-1" "border" "border-red-300" "bg-red-50" "rounded" "p-2"]}
     (for [err errors]
       [:div {:class ["flex" "items-start"]}
        [:span {:class ["mr-2"]} "•"]
        [:span {:class ["error-message"]} err]])]))

(defn login-form
  [{:keys [router values errors]}]
  [:form
   {:id "form-login"
    :class ["mx-auto" "max-w-lg"]
    :hx-post (ext/get-route router ::routes/login)
    :hx-target "#form-login"
    :hx-swap "outerHTML"}
   (ext/csrf-token-html)
   [:div {:class ["flex" "flex-col" "gap-4" "p-4" "md:p-8"]}
    (form-input {:input-name "email"
                 :input-type "email"
                 :input-value (:email values)
                 :errors (:email errors)
                 :required true
                 :props {:autocomplete "email"}})
    (form-input {:input-name "password"
                 :input-type "password"
                 :input-value (:password values)
                 :errors (:password errors)
                 :required true
                 :props {:autocomplete "new-password"}})
    (common-errors (:common errors))
    [:div {:class ["flex" "items-end" "justify-end" "py-2"]}
     [:a {:class ["text-indigo-500" "transition" "duration-100" "hover:text-indigo-600" "active:text-indigo-700"]
          :href (ext/get-route router ::routes/forgot-password)} "Forgot password?"]]
    [:button {:class ["block" "rounded-lg" "bg-gray-800" "px-8" "py-3" "text-center"
                      "text-sm" "font-semibold" "text-white" "outline-none" "ring-gray-300"
                      "transition" "duration-100" "hover:bg-gray-700" "focus-visible:ring"
                      "active:bg-gray-600" "md:text-base" "cursor-pointer"]} "Log in"]]
   [:div {:class ["flex" "items-center" "justify-center" "p-4"]}
    [:p {:class ["text-center" "text-sm" "text-gray-500"]}
     "Don't have an account? "
     [:a {:class ["text-indigo-500" "transition" "duration-100" "hover:text-indigo-600" "active:text-indigo-700"]
          :href (ext/get-route router ::routes/register)} "Register"]]]])

(defn login-page
  [{:keys [router]
    :as args}]
  (views/base
    [:div {:class ["bg-white" "py-6" "sm:py-8" "lg:py-12" "mt-20"]}
     [:nav {:class ["absolute" "top-0" "left-1/4" "p-4"]}
      [:div {:class ["flex" "gap-4"]}
       (views/button {:url (ext/get-route router ::routes/home)
                      :text "<- Home page"})]]
     [:div {:class ["mx-auto" "max-w-screen-2xl" "px-4" "md:px-8"]}
      [:h2 {:class ["mb-4" "text-center" "text-2xl" "font-bold" "text-gray-800" "md:mb-8" "lg:text-3xl"]} "Login"]
      (login-form args)]]))

(defn password-change-success
  []
  [:div {:class ["w-full" "max-w-md" "mx-auto" "p-6" "bg-green-50" "rounded-lg" "shadow-md"]}
   [:div {:class ["text-center"]}
    [:h3 {:class ["text-xl" "font-semibold" "text-green-800" "mb-2"]} "Password Updated Successfully"]
    [:p {:class ["text-green-700"]} "Your password has been changed."]]])

(defn change-password-form
  [{:keys [router values errors password-changed?]}]
  [:form {:id "form-change-password"
          :hx-post (ext/get-route router ::routes/change-password)
          :hx-swap "outerHTML"
          :hx-target "#form-change-password"
          :class ["space-y-4"]}
   (ext/csrf-token-html)
   (form-input {:input-name "current-password"
                :input-label "Current Password"
                :input-type "password"
                :input-value (:current-password values)
                :errors (:current-password errors)
                :required true})
   (form-input {:input-name "new-password"
                :input-label "New Password"
                :input-type "password"
                :input-value (:new-password values)
                :errors (:new-password errors)
                :required true})
   (form-input {:input-name "confirm-new-password"
                :input-label "Confirm New Password"
                :input-type "password"
                :input-value (:confirm-new-password values)
                :errors (:confirm-new-password errors)
                :required true})
   (common-errors (:common errors))
   (when password-changed?
     (password-change-success))

   [:button {:type "submit"
             :class ["block" "rounded-lg" "bg-gray-800" "px-8" "py-3" "text-center" "text-sm"
                     "font-semibold" "text-white" "outline-none" "ring-gray-300" "transition"
                     "duration-100" "hover:bg-gray-700" "focus-visible:ring" "active:bg-gray-600"
                     "md:text-base"]}
    "Update Password"]])

(defn account-page
  [{:keys [user router]
    :as args}]
  (views/base
    [:div {:class ["bg-white" "py-6" "sm:py-8" "lg:py-12" "mt-20"]}
     [:nav {:class ["absolute" "top-0" "left-1/4" "p-4"]}
      [:div {:class ["flex" "gap-4"]}
       (views/button {:url (ext/get-route router ::routes/home)
                      :text "<- Home page"})]]
     [:div {:class ["mx-auto" "max-w-screen-2xl" "px-4" "md:px-8"]}
      [:h2 {:class ["mb-4" "text-center" "text-2xl" "font-bold" "text-gray-800" "md:mb-8" "lg:text-3xl"]} "Account settings"]
      [:div {:class ["mx-auto" "max-w-lg"]}
       [:div {:class ["flex" "flex-col" "gap-4" "p-4" "md:p-8"]}
        [:h3 {:class ["text-2xl" "font-semibold" "text-gray-800" "mb-4"]} "User Information"]
        [:div {:class ["text-gray-800" "text-md" "font-semibold"]} (str "Email: " (:email user))]
        [:h3 {:class ["text-2xl" "font-semibold" "text-gray-800" "mb-4" "mt-12"]} "Change Password"]
        (change-password-form args)]]]]))

(defn forgot-password-form
  [{:keys [router values errors email-sent?]}]
  (if email-sent?
    [:p {:class ["text-center" "text-sm" "text-gray-500"]}
     "If you are a registered user, please check your email for the password reset link that we've sent you."]
    [:form
     {:id "form-forgot-password"
      :class ["mx-auto" "max-w-lg"]
      :hx-post (ext/get-route router ::routes/forgot-password)
      :hx-target "#form-forgot-password"
      :hx-swap "outerHTML"}
     (ext/csrf-token-html)
     [:p
      {:class ["text-center" "text-sm" "text-gray-500"]}
      "Enter your email address and we'll send you a link to reset your password."]
     [:div {:class ["flex" "flex-col" "gap-4" "p-4" "md:p-8"]}
      (form-input {:input-name "email"
                   :input-type "email"
                   :input-value (:email values)
                   :errors (:email errors)
                   :required true
                   :props {:autocomplete "email"}})
      [:button
       {:class ["block" "rounded-lg" "bg-gray-800" "px-8" "py-3" "text-center"
                "text-sm" "font-semibold" "text-white" "outline-none"
                "ring-gray-300" "transition" "duration-100" "hover:bg-gray-700"
                "focus-visible:ring" "active:bg-gray-600" "md:text-base"
                "cursor-pointer"]}
       "Send password reset instructions"]]]))

(defn forgot-password-page
  [{:keys [router]
    :as args}]
  (views/base
    [:div {:class ["bg-white" "py-6" "sm:py-8" "lg:py-12" "mt-20"]}
     [:nav {:class ["absolute" "top-0" "left-1/4" "p-4"]}
      [:div {:class ["flex" "gap-4"]}
       (views/button {:url (ext/get-route router ::routes/home)
                      :text "<- Home page"})]]
     [:div {:class ["mx-auto" "max-w-screen-2xl" "px-4" "md:px-8"]}
      [:h2 {:class ["mb-4" "text-center" "text-2xl" "font-bold" "text-gray-800" "md:mb-8" "lg:text-3xl"]} "Forgot your password?"]
      (forgot-password-form args)]]))

(defn reset-password-form
  [{:keys [router values errors token email]}]
  [:div {:id "form-reset-password"
         :class ["mx-auto" "max-w-screen-2xl" "px-4" "md:px-8"]}
   [:h2 {:class ["mb-4" "text-center" "text-2xl" "font-bold" "text-gray-800" "md:mb-8" "lg:text-3xl"]} "Reset Your Password"]
   [:p {:class ["text-center" "text-sm" "text-gray-500" "mb-4"]} "Enter a new password for " [:strong email]]
   [:form
    {:class ["mx-auto" "max-w-lg"]
     :hx-post (ext/get-route router ::routes/reset-password)
     :hx-target "#form-reset-password"
     :hx-swap "outerHTML"}
    (ext/csrf-token-html)
    [:input {:type "hidden"
             :name "token"
             :value token}]
    [:div {:class ["flex" "flex-col" "gap-4" "p-4" "md:p-8"]}
     (form-input {:input-name "password"
                  :input-label "New password"
                  :input-type "password"
                  :input-value (:password values)
                  :errors (:password errors)
                  :required true
                  :props {:autocomplete "new-password"}})
     (form-input {:input-name "confirm-password"
                  :input-label "Confirm password"
                  :input-type "password"
                  :input-value (:confirm-password values)
                  :errors (:confirm-password errors)
                  :required true
                  :props {:autocomplete "new-password"}})
     (common-errors (:common errors))
     [:button
      {:class ["block" "rounded-lg" "bg-gray-800" "px-8" "py-3" "text-center" "text-sm"
               "font-semibold" "text-white" "outline-none" "ring-gray-300" "transition"
               "duration-100" "hover:bg-gray-700" "focus-visible:ring" "active:bg-gray-600"
               "md:text-base" "cursor-pointer"]
       :type "submit"}
      "Reset Password"]]]])

(defn reset-password-page
  [{:keys [router token email]}]
  (views/base
    [:div {:class ["bg-white" "py-6" "sm:py-8" "lg:py-12" "mt-20"]}
     [:nav {:class ["absolute" "top-0" "left-1/4" "p-4"]}
      [:div {:class ["flex" "gap-4"]}
       (views/button {:url (ext/get-route router ::routes/home)
                      :text "<- Home page"})]]
     (reset-password-form {:router router
                           :token token
                           :email email})]))

(defn invalid-reset-token-page
  [{:keys [router]}]
  (views/base
    [:div {:class ["bg-white" "py-6" "sm:py-8" "lg:py-12" "mt-20"]}
     [:nav {:class ["absolute" "top-0" "left-1/4" "p-4"]}
      [:div {:class ["flex" "gap-4"]}
       (views/button {:url (ext/get-route router ::routes/home)
                      :text "<- Home page"})]]
     [:div {:class ["mx-auto" "max-w-screen-2xl" "px-4" "md:px-8"]}
      [:h2 {:class ["mb-4" "text-center" "text-2xl" "font-bold" "text-gray-800" "md:mb-8" "lg:text-3xl"]} "Invalid Reset Link"]
      [:div {:class ["mx-auto" "max-w-lg"]}
       [:div {:class ["flex" "flex-col" "gap-4" "p-4" "md:p-8"]}
        [:p {:class ["text-center" "text-gray-600"]} "The password reset link you used is invalid or has expired."]
        [:div {:class ["flex" "justify-center" "mt-4"]}
         [:a
          {:class ["text-indigo-500" "transition" "duration-100" "hover:text-indigo-600" "active:text-indigo-700"]
           :href (ext/get-route router ::routes/forgot-password)}
          "Request a new password reset link"]]]]]]))

(defn password-reset-success-page
  [{:keys [router]}]
  (views/base
    [:div {:class ["bg-white" "py-6" "sm:py-8" "lg:py-12" "mt-20"]}
     [:nav {:class ["absolute" "top-0" "left-1/4" "p-4"]}
      [:div {:class ["flex" "gap-4"]}
       (views/button {:url (ext/get-route router ::routes/home)
                      :text "<- Home page"})]]
     [:div {:class ["mx-auto" "max-w-screen-2xl" "px-4" "md:px-8"]}
      [:h2 {:class ["mb-4" "text-center" "text-2xl" "font-bold" "text-gray-800" "md:mb-8" "lg:text-3xl"]} "Password Reset Successfully"]
      [:div {:class ["mx-auto" "max-w-lg"]}
       [:div {:class ["flex" "flex-col" "gap-4" "p-4" "md:p-8"]}
        [:p {:class ["text-center" "text-gray-600"]} "Your password has been changed successfully."]
        [:div {:class ["flex" "justify-center" "mt-4"]}
         (views/button {:url (ext/get-route router ::routes/login)
                        :text "Log in with your new password"})]]]]]))
