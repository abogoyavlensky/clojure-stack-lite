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

  edn)
