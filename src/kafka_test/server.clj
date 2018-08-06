(ns kafka-test.server
  (:gen-class)
  (:use org.httpkit.server)
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

(def xrequest (atom [])) ; debug

(def topic "kafka-test") ;debug


(def new-id (atom 0))
(def kafka-observables (atom {}))
(def state (atom {}))

(defn get-filter [request]
    (let [id (request :id)]
        (swap! xrequest (fn [x] (conj x request)))
        (if (nil? id) (vals @state) (@state id))))

(defn post-filter [request]
  (let [filter (request :body)
        id (swap! new-id inc)]
        (swap! state #(add-filter % id (filter :topic) (filter :match)))
        (let [topic (filter :topic)]
          (if (contains? @kafka-observables topic)
            ()
            (let [kafka-messages (rx-kafka-messages topic)]
              (swap! kafka-observables #(merge % {topic kafka-messages}))
              (rx/on-value kafka-messages (fn [message] (swap! state #(match-message % message))))))
        ;(swap! kafka-observables #())
    "OK")))

(defn delete-filter [request]
  (let [id (request :id)]
    (swap! state #(remove-filter % id))
    "OK"))

(defroutes all-routes
  (GET "/" [] "Hello!")
  (GET "/filter" [] get-filter)
  (POST "/filter" [] post-filter)
;   (context "/filter:id" [id]
;      (GET / [] (fn [req] ))
;      (DELETE / [] delete-filter))
  (route/resources "/")
  (route/not-found "Resource not found") ;; static file url prefix /static, in `public` folder
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