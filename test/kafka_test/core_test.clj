(ns kafka-test.core-test
  (:require [clojure.test :refer :all]
            [kafka-test.core :refer :all]))

(deftest add-filter-test
  (testing "merges new filter into existing state"
    (is (=
         {1 {:id 1 :topic "test" :match "beaver" :messages []}}
         (add-filter {} 1 "test" "beaver")))))

(deftest match-message-test
  (testing "adds new message for each filter that match"
    (let [state {1 {:id 1 :topic "test" :match "resistance"}
                 2 {:id 2 :topic "test" :match "stance" :messages ["perfect stance"]}
                 3 {:id 3 :topic "test" :match "none"}
                 4 {:id 4 :topic "test2" :match "stance"}}]
     (is (=
         {1 {:id 1 :topic "test" :match "resistance" :messages ["resistance is calling"]}
          2 {:id 2 :topic "test" :match "stance" :messages ["perfect stance" "resistance is calling"]}
          3 {:id 3 :topic "test" :match "none"}
          4 {:id 4 :topic "test2" :match "stance"}}
         (match-message state {:topic "test" :value "resistance is calling"})))
      ) 
    ))

(deftest remove-filter-test
  (testing "removes filter from state by id"
    (is (=
         {2 {:id 2 :topic "test2" :match "rabbit"}}
         (remove-filter
          {1 {:id 1 :topic "test" :match "beaver"}
           2 {:id 2 :topic "test2" :match "rabbit"}}
          1)))))