(ns kafka-test.core)

(defn add-filter [state id topic match]
  (merge state {id {:id id :topic topic :match match :messages []}}))

(defn remove-filter [state id]
  (dissoc state id))

(defn match? [str match]
  (.contains
    (clojure.string/lower-case str)
    (clojure.string/lower-case match)))

(defn match-message [state message]
  (let [filters (map (fn [filter]
    (if (and
      (.equals (message :topic) (filter :topic))
      (match? (message :value) (filter :match)))
        (merge filter {:messages (conj (filter :messages) (message :value))})
        filter))
      (vals state))]
    (->> filters
         (map #(hash-map (:id %) %))
         (into {}))))