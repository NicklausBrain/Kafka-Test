(ns kafka-test.core
  (require [beicon.core :as rx]))

(defn add-filter [state id topic match listen-kafka match-message]
  (let [state
    (if-not (contains? (state :subjects) topic)
      (let [new-subject (listen-kafka topic)]
        (rx/on-value new-subject match-message) ; todo: test it
        (merge state {:subjects {topic new-subject}}))
      state)]
    (merge-with into state {:filters {id {:id id :topic topic :match match :messages []}}})))

(defn remove-filter [state id]
  (dissoc state id))

(defn match? [str match]
  (.contains
    (clojure.string/lower-case str)
    (clojure.string/lower-case match)))

(defn match-message [state message]
  (let [filters (map
    (fn [filter]
      (if (and
        (.equals (message :topic) (filter :topic))
        (match? (message :value) (filter :match)))
        (merge filter {:messages (conj (filter :messages) (message :value))})
        filter))
      (vals (state :filters)))]
    (merge state {:filters (->> filters (map #(hash-map (:id %) %)) (into {}))})))