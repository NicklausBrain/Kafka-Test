(ns kafka-test.kafka (:gen-class)
  (import org.apache.kafka.common.serialization.StringSerializer)
  (import org.apache.kafka.common.serialization.StringDeserializer)
  (import org.apache.kafka.clients.consumer.KafkaConsumer)
  (import org.apache.kafka.common.TopicPartition)
  (import java.util.Arrays)
  (require [beicon.core :as rx]))

;(def jaasTemplate "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";")
;(def brokers (System/getenv "bootstrap.servers"))
; (def username (System/getenv "CLOUDKARAFKA_USERNAME"))
; (def password (System/getenv "CLOUDKARAFKA_PASSWORD"))
(def topic "kafka-test")

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


;(.seekToBeginning consumer partitions)
;(rx/on-value c #(println "v:" %))
(defn new-consumer []
    (rx/from-coll (fn [sink]
        (.start (Thread. (fn []
            (let [consumer (new KafkaConsumer (init-kafka-props))
                  topicPartition (new TopicPartition topic 0)
                  partitions (list topicPartition)]
                  (.assign consumer partitions)
                  (while true (do
                    (for [record (seq (.poll consumer 100))] (sink (.value record)))))
              ))))
            ;(sink (rx/end 0))
        (fn []
              ;; function called on unsubscription
        )
)))


;TopicPartition topicPartition = new TopicPartition (topic, 0);
  ;List partitions = Arrays.asList(topicPartition);
  ;consumer.assign(partitions);
  ;consumer.seekToBeginning(partitions);

  ; (let [consumer (new KafkaConsumer (init-kafka-props brokers username password))
  ;       topicPartition (new TopicPartition topic 0)
  ;       partitions (list topicPartition)]
  ;   ;(println "Hello " username)
  ;   ;(.assign consumer partitions)
  ;   ;(.seekToBeginning consumer partitions)
  ;   (.subscribe consumer (list topic))
  ;   ;(while true (do
  ;   (for [record (seq (.poll consumer 100))] 
  ;       (println
  ;         (format "%s [%d] offset=%d, key=%s, value=\"%s\"\n"
  ;         (.topic record)
  ;         (.partition record)
  ;         (.offset record)
  ;         (.key record)
  ;         (.value record))))
      ;))
  ;  )

;(def c new-consumer)
;(rx/on-value c #(println "v:" %))

; (import org.apache.kafka.common.serialization.StringSerializer)
; (import org.apache.kafka.common.serialization.StringDeserializer)
; (import org.apache.kafka.clients.consumer.KafkaConsumer)
; (import org.apache.kafka.common.TopicPartition)
; (import java.util.Arrays)

; (def consumer (new KafkaConsumer (init-kafka-props)))
; (def topicPartition (new TopicPartition topic 0))
; (def partitions (list topicPartition))
; (.assign consumer partitions)