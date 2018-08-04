(ns kafka-test.core (:gen-class))

(defn -main [n]
  (def start (System/currentTimeMillis))
  (println "Hello world!")
  (def end (System/currentTimeMillis))
  (println (str "Time spent: " (- end start)))
)

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
