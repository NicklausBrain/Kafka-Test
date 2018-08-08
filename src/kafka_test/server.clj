(ns kafka-test.server
  (:gen-class)
  (:use org.httpkit.server)
  (require [clojure.string :as str])
  (require [kafka-test.kafka :refer :all])
  (require [kafka-test.core :refer :all])
  (require [ring.middleware.params :refer :all])
  (require [beicon.core :as rx])
  (use ruiyun.tools.timer)
  (use ring.util.response)
  (require [compojure.core :refer :all]
           [compojure.route :as route]
           [ring.middleware.defaults :refer :all]
           [ring.middleware.json :as middleware]
           [ring.middleware.cors :refer [wrap-cors]]))

(def new-id (atom 0))
(def kafka-observables (atom {})) ;move to state
; I assume it is possible to get rid of this state but I didn't get how to combine 
; multiple observables into single backpressure-aware observable (passing state as parameter)
(def state (atom {}))

(defn parse-query-string [qs]
  (if (> (count qs) 0)
    (apply hash-map (str/split qs #"[&=]"))))

(defn get-filter [request]
    (let [query-string (parse-query-string (request :query-string))
          id (if (nil? query-string) nil (query-string "id"))]
        (if (str/blank? id)
          (vals @state)
          (str (@state (. Integer parseInt id))))))

(defn post-filter [request]
  (let [filter (request :body)
        id (swap! new-id inc)
        topic (filter :topic)]
        (swap! state #(add-filter % id topic (filter :match)))
        (try-add-observable-topic topic kafka-observables listen-kafka
            (fn [message] (swap! state #(match-message % message))))
    "OK"))

(defn delete-filter [request]
  (let [id ((request :body) :id)]
    (swap! state #(remove-filter % id))
    "OK"))

(defroutes all-routes
  (GET "/" [] "Hello!")
  (GET "/filter" [] get-filter)
  (POST "/filter" [] post-filter)
  (DELETE "/filter" [] delete-filter)
  (route/resources "/")
  (route/not-found "Resource not found")
)

(def app (->
  all-routes
  (wrap-cors :access-control-allow-origin [#".*"]
             :access-control-allow-methods [:get :put :post :delete])
  (middleware/wrap-json-body {:keywords? true :bigdecimals? true})
  (middleware/wrap-json-response)
))

(defonce server (atom nil))

(defn stop-server [] ; debug
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn -main [& args]
  (def start (System/currentTimeMillis))

  (reset! server (run-server #'app {:port 8080}))

  (def end (System/currentTimeMillis))
  (println (str "Time spent: " (- end start))))