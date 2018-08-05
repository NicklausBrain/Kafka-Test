(ns kafka-test.core
  (:gen-class)
  (:use org.httpkit.server)
  (:use [compojure.route :only [files not-found]]
        [compojure.handler :only [site]] ; form, query params decode; cookie; session, etc
        [compojure.core :only [defroutes GET POST DELETE ANY context]]
        org.httpkit.server)
 (:require [kinsky.client      :as client]
           [kinsky.async       :as async]
           [clojure.core.async :as a :refer [go <! >!]])
  
  (require [kafka-test.kafka :refer :all])
  (require [beicon.core :as rx])
  (use ruiyun.tools.timer))

(defroutes all-routes
  (GET "/" [] "Hello world")
  ;(GET "/ws" [] chat-handler)     ;; websocket
  ;(GET "/async" [] async-handler) ;; asynchronous(long polling)
  ;(context "/user/:id" []
    ;(GET / [] get-user-by-id)
    ;(POST / [] update-userinfo))
  (files "/static/") ;; static file url prefix /static, in `public` folder
  (not-found "<p>Resource not found.</p>")) ;; all other, return 404

(defn app [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "hello HTTP!"})


(defonce server (atom nil))

(defn stop-server []
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server :timeout 100)
    (reset! server nil)))

(defn -main [& args]
  (def start (System/currentTimeMillis))
  
  (run-server (site #'all-routes) {:port 8080})
  ;(reset! server (run-server #'app {:port 8080}))

  (def end (System/currentTimeMillis))
  (println (str "Time spent: " (- end start))))