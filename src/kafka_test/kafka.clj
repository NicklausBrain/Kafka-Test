(ns kafka-test.kafka (:gen-class)
  (import org.apache.kafka.common.serialization.StringSerializer)
  (import org.apache.kafka.common.serialization.StringDeserializer)
  (import org.apache.kafka.clients.consumer.KafkaConsumer)
  (import org.apache.kafka.common.TopicPartition)
  (import java.util.Arrays)
  (require [beicon.core :as rx])
  (use ruiyun.tools.timer))

;(def jaasTemplate "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";")
;(def brokers (System/getenv "bootstrap.servers"))
; (def username (System/getenv "CLOUDKARAFKA_USERNAME"))
; (def password (System/getenv "CLOUDKARAFKA_PASSWORD"))

(defn init-kafka-props
  []
  (doto (new java.util.Properties)
    (.put "bootstrap.servers", "mbrain.top:9092")
    (.put "group.id", "clojure")
    (.put "enable.auto.commit", "true")
    (.put "auto.commit.interval.ms", "1000")
    (.put "auto.offset.reset", "earliest")
    (.put "session.timeout.ms", "1000")
    (.put "heartbeat.interval.ms", "500")
    (.put "key.deserializer", (.getName StringDeserializer))
    (.put "value.deserializer", (.getName StringDeserializer))
    (.put "key.serializer", (.getName StringSerializer))
    (.put "value.serializer", (.getName StringSerializer))
    ;(.put "security.protocol", "SASL_SSL")
    ;(.put "sasl.mechanism", "SCRAM-SHA-256")
    ;(.put "sasl.jaas.config", (format jaasTemplate username password)
))

(defn rx-kafka-messages [topic]
  (let [consumer (new KafkaConsumer (init-kafka-props))
        topicPartition (new TopicPartition topic 0)
        partitions (list topicPartition)]
    (.assign consumer partitions)
    (rx/create (fn [sink]
        (run-task! (fn []
            (let [records (for [record (seq (.poll consumer 100))] (.value record))]
                (if (empty? records) () (doall (map sink records))))) :period 500)
        (fn []
          ;; function called on unsubscription
        )))))