(ns double-booking.core-test
  (:require [clojure.test :refer :all]
            [clj-time.core :as t]
            [clj-time.coerce :as c]
            [org.dthume.data.interval-treeset :as it]
            [double-booking.core :refer :all]
            [clojure.pprint :refer [pprint]]))

;; Utility functions to convert event/treeset into readable format
(defn- date->long
  [year m d]
  (c/to-long (t/date-time year m d)))

(defn- view-treeset
  [ts]
  (mapv (fn [[f t]] [(c/from-long f) (c/from-long t)]) ts))

(defn- view-event
  [[f t]]
  [(c/from-long f) (c/from-long t)])

;; events fixtures
(def e-1-2 [(date->long 2000 1 1) (date->long 2000 1 2)])
(def e-1-2-1 [(date->long 2000 1 1) (+ (date->long 2000 1 2) 1)])
(def e-1-2-2 [(date->long 2000 1 1) (+ (date->long 2000 1 2) 2)])
(def e-3-4 [(date->long 2000 1 3) (date->long 2000 1 4)])
(def e-3-4-1 [(date->long 2000 1 3) (+ (date->long 2000 1 4) 1)])
(def e-5-6 [(date->long 2000 1 5) (date->long 2000 1 6)])
(def e-7-8 [(date->long 2000 1 7) (date->long 2000 1 8)])

(def e-1-4 [(date->long 2000 1 1) (date->long 2000 1 4)])
(def e-1-8 [(date->long 2000 1 1) (date->long 2000 1 8)])

(def e-no-overlapp [(date->long 2000 2 1) (date->long 2000 2 2)])

;; interval trees fixtures
(def ts (into (it/interval-treeset) [e-1-2 e-3-4 e-5-6 e-7-8]))
(def ts-one-overlap (into (it/interval-treeset) (conj ts e-1-2-1)))
(def ts-two-overlap (into (it/interval-treeset) (conj ts-one-overlap e-1-2-2)))
(def ts-three-overlap (into (it/interval-treeset) (conj ts-two-overlap e-3-4-1)))

(deftest overlapping-test
  (testing "2000-1-1 ~ 2000-1-2 is overlapped"
    (is (= [e-1-2]
           (overlapping-events ts e-1-2))))

  (testing "2000-2-1 ~ 2000-2-2  is not overlapped"
    (is (= nil
           (overlapping-events ts e-no-overlapp))))

  (testing "2000-1-1 ~ 2000-1-4 has TWO overlapped events"
    (is (= [e-1-2 e-3-4]
           (overlapping-events ts e-1-4))))

  (testing "2000-1-1 ~ 2000-1-8 has FOUR overlapped events"
    (is (= ts
           (overlapping-events ts e-1-8)))))

(deftest all-overlapping-events-test
  (testing "There is ZERO overlapping event"
    (is (= '()
           (all-overlapping-events ts))))

  (testing "There is ONE overlapping event"
    (is (= (into (it/interval-treeset) [e-1-2 e-1-2-1])
           (all-overlapping-events ts-one-overlap))))

  (testing "There are TWO overlapping events"
    (is (= (into (it/interval-treeset) [e-1-2 e-1-2-1 e-1-2-2])
           (all-overlapping-events ts-two-overlap))))

  (testing "There are THREE overlapping events"
    (is (= (into (it/interval-treeset) [e-1-2 e-1-2-1 e-1-2-2 e-3-4 e-3-4-1])
           (all-overlapping-events ts-three-overlap)))))
