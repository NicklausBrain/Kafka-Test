(ns kafka-test.core
  (:gen-class)
  (:use org.httpkit.server)
  (require [kafka-test.kafka :refer :all])
  (require [beicon.core :as rx])
  (use ruiyun.tools.timer)
  (use ring.util.response)
  (require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.json :as middleware]
            [ring.middleware.cors :refer [wrap-cors]])
)

(def state (atom {:v 1 :filters {}}))

(def filter-additions (rx/subject))
(def filter-deletions (rx/subject))

(defn add-filter [state id topic match]
  (merge state {id {:id id :topic topic :match match :messages []}}))

(defn remove-filter [state id]
  (dissoc state id))

(defn match? [str match]
  (.contains
    (clojure.string/lower-case str)
    (clojure.string/lower-case match)))

(defn match-message [state message]
  (let [filters
    (map
      (fn [filter]
        (if (and
          (.equals (message :topic) (filter :topic))
          (match? (message :value) (filter :match)))
            (merge filter {:messages (conj (filter :messages) (message :value))})
            filter))
      (vals state))]
    (->> filters
         (map #(hash-map (:id %) %))
         (into {}))))

(defn postFilter [req] (println "req:" req))

(defroutes all-routes
  (GET "/" [] "Hello!")
  (POST "/filter" postFilter)
  ; (context "/filter:id" []
  ;   (GET / [] "dummyfilter")
  ;   (POST / [] postFilter))
  (route/resources "/")
  (route/not-found "Resource not found") ;; static file url prefix /static, in `public` folder
  ) ;; all other, return 404

(def app (->
          all-routes
          (wrap-cors :access-control-allow-origin [#".*"]
                     :access-control-allow-methods [:get :put :post :delete])
          (middleware/wrap-json-body {:keywords? true :bigdecimals? true})
          (middleware/wrap-json-response)
          ;(wrap-defaults site-defaults)
          ))

(defonce server (atom nil))

(defn stop-server []
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server :timeout 100)
    (reset! server nil)))

(def topic "kafka-test")

(defn -main [& args]
  (def start (System/currentTimeMillis))

  ;(run-server (site #'all-routes) {:port 8080})
  ;(reset! server (run-server #'app {:port 8080}))
  (reset! server (run-server #'app {:port 8080}))

  (def end (System/currentTimeMillis))
  (println (str "Time spent: " (- end start))))

