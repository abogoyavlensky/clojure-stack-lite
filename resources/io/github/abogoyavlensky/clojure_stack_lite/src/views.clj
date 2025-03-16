(ns {{main/ns}}.views
  (:require [manifest-edn.core :as manifest]))

(defn base
  "Base component for html page."
  [content]
  [:html
   {:lang "en"}
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1, maximum-scale=1, user-scalable=0"}]
    [:meta {:name "msapplication-TileColor"
            :content "#ffffff"}]
    [:link {:rel "manifest"
            :href "/assets/manifest.json"}]
    [:link {:rel "icon"
            :href (manifest/asset "images/icon@32px.png")}]
    [:link {:rel "icon"
            :href (manifest/asset "images/icon.svg")
            :type "image/svg+xml"}]
    [:link {:rel "apple-touch-icon"
            :sizes "180x180"
            :href (manifest/asset "images/icon@180px.png")}]
    [:link {:type "text/css"
            :href (manifest/asset "css/output.css")
            :rel "stylesheet"}]
    [:title "Clojure Stack Lite | A Template for Clojure Projects"]]
   [:body
    content
    [:script {:type "text/javascript"
              :src (manifest/asset "js/htmx.min.js")
              :defer true}]
    [:script {:type "text/javascript"
              :src (manifest/asset "js/alpinejs.min.js")
              :defer true}]]])

(defn error-page
  [text]
  (base
    [:div {:class ["mt-56"]}
     [:div {:class ["mx-auto" "text-center"]}
      [:h1 {:class ["text-5xl"]} text]]]))

(def home-page
  (base
    ; ========= TODO: Update home page  ========================
    [:div
     {:class ["text-slate-800" "min-h-screen" "flex" "flex-col"]}
     [:main {:class ["flex-grow" "flex" "items-center" "justify-center"]}
      [:div {:class ["container" "mx-auto" "px-4" "max-w-4xl" "text-center"]}
       [:h1 {:class ["text-6xl" "font-bold" "mb-6" "text-slate-900"]} "Welcome to "
        [:span {:class ["bg-gradient-to-r" "from-emerald-400" "via-sky-400" "to-sky-200" "bg-clip-text" "text-transparent" "relative"]}
         "Clojure Stack Lite"]]
       [:p {:class ["text-2xl" "mb-10" "text-slate-600"]} "A lightweight, modern template to jumpstart your Clojure projects"]
       [:p {:class ["text-lg" "mb-12" "text-slate-500"]}
        "To begin, modify the existing view in " [:code {:class ["bg-slate-100" "px-1" "rounded"]} "src/yourproject/views/home.clj"]
        " or add a new route in " [:code {:class ["bg-slate-100" "px-1" "rounded"]} "src/yourproject/routes.clj"]
        " and define a handler in " [:code {:class ["bg-slate-100" "px-1" "rounded"]} "src/yourproject/handlers.clj"]]
       [:div {:class ["mb-16"]}
        [:a {:class ["bg-slate-900" "hover:bg-slate-800" "text-white" "font-medium" "py-3" "px-8" "rounded-lg" "transition-colors" "duration-200" "mr-4"]
             :href "https://github.com/bogoyavlensky/clojure-stack-lite"} "Get Started"]]]]
     [:footer {:class ["py-6" "text-center" "text-sm" "text-slate-500"]}
      [:p "Made with ❤️ for the Clojure community"]]]))
