(defproject kafka-test "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
    [org.clojure/clojure "1.8.0"]
    [org.apache.kafka/kafka-clients "1.0.0"]
    [org.apache.kafka/kafka_2.11 "1.0.0"]
    [ring "1.7.0-RC1"]
    [compojure "1.6.1"]
    [http-kit "2.2.0"]
    [spootnik/kinsky "0.1.22"]
    [funcool/beicon "4.1.0"]
    ;[org.slf4j/log4j-over-slf4j "1.7.25"]
    ]
  :main kafka-test.core)
