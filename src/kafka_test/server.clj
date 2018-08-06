(ns kafka-test.server
  (:gen-class)
  (:use org.httpkit.server)
  (require [kafka-test.kafka :refer :all])
  (require [kafka-test.core :refer :all])
  (require [beicon.core :as rx])
  (use ruiyun.tools.timer)
  (use ring.util.response)
  (require [compojure.core :refer :all]
           [compojure.route :as route]
           [ring.middleware.defaults :refer :all]
           [ring.middleware.json :as middleware]
           [ring.middleware.cors :refer [wrap-cors]]))

(def new-id (atom 0))
(def state (atom {}))
(def request (atom [])) ; debug

(defn post-filter [request]
  (let [filter (request :body)
        id (swap! new-id inc)]
    (swap! state #(add-filter % id (filter :topic) (filter :match)))
    "OK"))

(defroutes all-routes
  (GET "/" [] "Hello!")
  (GET "/filter" [] (vals @state))
  (POST "/filter" [] post-filter)
  ; (context "/filter:id" []
  ;   (GET / [] "dummyfilter")
  ;   (POST / [] postFilter))
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

(defn stop-server []
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server :timeout 100)
    (reset! server nil)))

(def topic "kafka-test") ;debug

(defn -main [& args]
  (def start (System/currentTimeMillis))

  (reset! server (run-server #'app {:port 8080}))

  (def end (System/currentTimeMillis))
  (println (str "Time spent: " (- end start))))
