(ns kafka-test.core (:gen-class)
  (import org.apache.kafka.common.serialization.StringSerializer)
  (import org.apache.kafka.common.serialization.StringDeserializer)
  (import org.apache.kafka.clients.consumer.KafkaConsumer)
  (import org.apache.kafka.common.TopicPartition)
  (import java.util.Arrays))

(def jaasTemplate "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";")

(def brokers (System/getenv "CLOUDKARAFKA_BROKERS"))
(def username (System/getenv "CLOUDKARAFKA_USERNAME"))
(def password (System/getenv "CLOUDKARAFKA_PASSWORD"))

(def topic (str username "-default"))

(defn init-kafka-props
  [brokers username password]
    (doto (new java.util.Properties)
      (.put "bootstrap.servers", brokers)
      (.put "group.id", (str username "-consumer"))
      (.put "enable.auto.commit", "true")
      (.put "auto.commit.interval.ms", "1000")
      (.put "auto.offset.reset", "earliest")
      (.put "session.timeout.ms", "10000")
      (.put "key.deserializer", (.getName StringDeserializer))
      (.put "value.deserializer", (.getName StringDeserializer))
      (.put "key.serializer", (.getName StringSerializer))
      (.put "value.serializer", (.getName StringSerializer))
      (.put "security.protocol", "SASL_SSL")
      (.put "sasl.mechanism", "SCRAM-SHA-256")
      (.put "sasl.jaas.config", (format jaasTemplate username password))))

(defn -main []
  (def start (System/currentTimeMillis))
  ;TopicPartition topicPartition = new TopicPartition (topic, 0);
  ;List partitions = Arrays.asList(topicPartition);
  ;consumer.assign(partitions);
  ;consumer.seekToBeginning(partitions);

  (let [brokers (System/getenv "CLOUDKARAFKA_BROKERS")
        username (System/getenv "CLOUDKARAFKA_USERNAME")
        password (System/getenv "CLOUDKARAFKA_PASSWORD")
        consumer (new KafkaConsumer (init-kafka-props brokers username password))
        topicPartition (new TopicPartition topic 0)
        partitions (list topicPartition)]
    ;(println "Hello " username)
    ;(.assign consumer partitions)
    ;(.seekToBeginning consumer partitions)
    (.subscribe consumer (list topic))
    ;(while true (do
    (for [record (seq (.poll consumer 100))] 
        (println
          (format "%s [%d] offset=%d, key=%s, value=\"%s\"\n"
          (.topic record)
          (.partition record)
          (.offset record)
          (.key record)
          (.value record))))
      ;))
    )

  (def end (System/currentTimeMillis))
  (println (str "Time spent: " (- end start))))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
