(ns kafka-test.server (:gen-class)
  (use org.httpkit.server)
  (use ruiyun.tools.timer)
  (use ring.util.response)
  (require [clojure.string :as str])
  (require [kafka-test.kafka :refer :all])
  (require [kafka-test.core :refer :all])
  (require [ring.middleware.params :refer :all])
  (require [beicon.core :as rx])
  (require [compojure.core :refer :all]
           [compojure.route :as route]
           [ring.middleware.defaults :refer :all]
           [ring.middleware.json :as middleware]
           [ring.middleware.cors :refer [wrap-cors]])))

(def new-id (atom 0))
(def state (rx/behavior-subject {}))
(def actions (rx/behavior-subject (.getValue state)))
(def transformations (rx/scan
  (fn [state action] (if (fn? action) (action state) state))
  actions))

(rx/on-value transformations (fn [new-state] (rx/push! state new-state)))

(defn parse-query-string [qs]
  (if (> (count qs) 0)
    (apply hash-map (str/split qs #"[&=]"))))

(defn get-filter [request]
  (let [query-string (parse-query-string (request :query-string))
        id (if-not (nil? query-string) (query-string "id"))]
    (if (str/blank? id)
      (vals ((.getValue state) :filters))
      (str (((.getValue state) :filters) (. Integer parseInt id))))))

(defn post-filter [request]
  (let [filter (request :body)
        id (swap! new-id inc)
        topic (filter :topic)]
    (rx/push! actions 
      (fn [old-state]
        (add-filter old-state id topic (filter :match) listen-kafka
          (fn [message]
            (rx/push! actions
              (fn [old-state] (match-message old-state message)))))))
    "OK"))

(defn delete-filter [request]
  (let [id ((request :body) :id)]
    (rx/push! actions (fn [old-state] (remove-filter old-state id)))
    "OK"))

(defroutes all-routes
  (GET "/" [] "Hello!")
  (GET "/filter" [] get-filter)
  (POST "/filter" [] post-filter)
  (DELETE "/filter" [] delete-filter)
  (route/resources "/")
  (route/not-found "Resource not found"))

(def app (-> all-routes
  (wrap-cors :access-control-allow-origin [#".*"]
             :access-control-allow-methods [:get :put :post :delete])
  (middleware/wrap-json-body {:keywords? true :bigdecimals? true})
  (middleware/wrap-json-response)))

(defonce server (atom nil))

(defn stop-server [] ; debug
  (when-not (nil? @server)
    (@server :timeout 100)
    (reset! server nil)))

(defn -main [& args]
  (let [port 8080]
    (reset! server (run-server #'app {:port port}))
    (println "server started on " port)))

; (require [clojure.reflect :as r])
; (use [clojure.pprint :only [print-table]]