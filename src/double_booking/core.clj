(ns double-booking.core
  (:require [org.dthume.data.interval-treeset :as it]
            [org.dthume.data.set :as ds]))

(defn overlapping-events
  "Returns a overlapping event within the given event-set"
  [event-set event]
  (->> event
       (it/select-overlapping event-set)
       second
       not-empty))

(defn all-overlapping-events
  "Returns a sequence of all pairs of overlapping events "
  [event-set]
  (->> event-set
       (mapcat (fn [e]
                 (->> e
                      (it/select-overlapping event-set)
                      second
                      ((fn [x] (when (< 1 (count x)) x))))))
       (into (it/interval-treeset))))
