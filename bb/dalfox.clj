(ns dalfox
  (:require [babashka.process :as process]
            [cheshire.core :as json]))

(defn- -run [name]
  (some-> (process/shell {:out :string :err :string}
                         "dalfox" "-X" "POST" "url" (str "http://localhost:8080/" name)
                         "--skip-discovery" "-p" "name" "-p" "color" "-p" "image"
                         "-S"
                         "--format" "json")
          :out
          (json/decode keyword)))

(defn- -generate-msg [name {:keys [message_str]}]
  (str "::error::" "[" name "] " message_str))

(defn run [name]
  (let [results (remove empty? (-run name))]
    (doseq [r results]
      (println (-generate-msg name r)))
    (when (seq results)
      (System/exit 1))))
