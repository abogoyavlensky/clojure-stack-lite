(ns io.github.abogoyavlensky.clojure-stack-lite)

(defn data-fn
  "Example data-fn handler.

  Result is merged onto existing options data.
  Returning nil means no changes to options data."
  [_data])

(defn post-process-fn
  "Example post-process-fn handler.

  Can programmatically modify files in the generated project."
  [edn data]
  (when (true? (:debug data))
    (println "post-process-fn not modifying" (:target-dir data))))

(def RENAMINGS
  {:daisyui {"resources_public_css_default" "resources_public_css_daisyui"}})

(def EXTENSIONS
  {:daisyui [["resources_public_js_daisyui" "resources/public/js"]]})

(defn- apply-transform-source-dir
  [suffix transform]
  (let [transform-renamed (reduce
                            (fn [acc v]
                              (let [origin-source-dir (first v)
                                    new-source-dir (get-in RENAMINGS [suffix origin-source-dir])]
                                (if (some? new-source-dir)
                                  (conj acc (assoc v 0 new-source-dir))
                                  (conj acc v))))
                            []
                            transform)]
    (concat transform-renamed (get EXTENSIONS suffix))))

; Transform dirs
(defn template-fn
  "Example template-fn handler.

  Result is used as the EDN for the template."
  [edn data]
  (when (true? (:debug data))
    (println "template-fn has got data:")
    (prn data)
    (println "template-fn returning edn:")
    (prn edn))

  (let [new-transform (cond->> (:transform edn)
                        (:daisyui data) (apply-transform-source-dir :daisyui))]
    (assoc edn :transform new-transform)))
