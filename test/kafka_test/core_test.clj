(ns kafka-test.core-test
  (:require [clojure.test :refer :all]
            [kafka-test.core :refer :all]
            [beicon.core :as rx]))

(deftest add-filter-tests
  (testing "merges new filter into empty state"
    (let [kafka-subject (rx/subject)
          listen-kafka (fn [topic] kafka-subject)
          state {}]
      (is (=
        {:subjects {"test" kafka-subject}
         :filters {1 {:id 1 :topic "test" :match "beaver" :messages []}}}
        (add-filter state 1 "test" "beaver" listen-kafka #())))))
  (testing "does not duplicate subjects"
    (let [old-kafke-subject (rx/subject)
          new-kafka-subject (rx/subject)
          listen-kafka (fn [topic] new-kafka-subject)
          state {:subjects {"test" old-kafke-subject}
                 :filters {0 {:id 0 :topic "test" :match "bionic" :messages []}}}]
      (is (=
           {:subjects {"test" old-kafke-subject}
            :filters {0 {:id 0 :topic "test" :match "bionic" :messages []}
                      1 {:id 1 :topic "test" :match "beaver" :messages []}}}
           (add-filter state 1 "test" "beaver" listen-kafka #()))))))

(deftest match-message-test
  (testing "adds new message for each filter that match"
    (let [state {:filters {1 {:id 1 :topic "test" :match "resistance"}
                           2 {:id 2 :topic "test" :match "stance" :messages ["perfect stance"]}
                           3 {:id 3 :topic "test" :match "none"}
                           4 {:id 4 :topic "test2" :match "stance"}}
                 :subjects {"test" {} "test2" {}}}]
     (is (=
         {:filters {1 {:id 1 :topic "test" :match "resistance" :messages ["resistance is calling"]}
                    2 {:id 2 :topic "test" :match "stance" :messages ["perfect stance" "resistance is calling"]}
                    3 {:id 3 :topic "test" :match "none"}
                    4 {:id 4 :topic "test2" :match "stance"}}
          :subjects {"test" {} "test2" {}}}
         (match-message state {:topic "test" :value "resistance is calling"}))))))

(deftest remove-filter-test
  (testing "removes filter from state by id"
    (is (=
         {2 {:id 2 :topic "test2" :match "rabbit"}}
         (remove-filter
          {1 {:id 1 :topic "test" :match "beaver"}
           2 {:id 2 :topic "test2" :match "rabbit"}}
          1)))))