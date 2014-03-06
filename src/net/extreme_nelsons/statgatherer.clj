(ns net.extreme-nelsons.statgatherer
    (:require [clojure.pprint :as pp]
            [clj-time.format :refer [formatters parse]]
            [clj-time.core :refer [before? date-time today minus- days weeks months within? local-date]]
            [clojure.string :refer [join]]
            [clojure.pprint :refer [pprint]]
            [net.extreme-nelsons.state :refer
             [update-state get-state get-whole-state]]))

(def bifDate (formatters :date))
(def bifDateTime (formatters :date-time))

(defn parse-todays-date
  "Get todays date as a datetime object as of midnight."
  []
  (let [td (today)]
    (date-time (:year td) (:month td) (:day td))))

(defn get-processed-date
  ""
  [entry]
  (:revdate entry))

(defn get-authors
  ""
  []
  (let [tree (get-state :processed-data)]
    (into #{} (map :author tree))))

(defn get-revision-list
  ""
  []
  (let [tree (get-state :processed-data)]
    (into [] (map :revnum tree))))

(defn revdate-cond
  ""
  [start end entry]
  (println "In the partial")
  (let [thedate (:revdate entry)]
    (println (join " " [start end thedate]))
    (within? start end thedate)))

(defn get-all-in-date-range
  ""
  [start end]
  (let [state (get-state :processed-data)]
    (map #(when (within? end start (org.joda.time.LocalDate. (:revdate %1))) %1) state)
    )
  )

(defn get-n-days-from-today
  ""
  [num-days]
  (let [end (minus- (today) (days num-days))]
    (println (join " " [num-days end]))
    (get-all-in-date-range (today) end)
    )
  )

(defn timeperiod-stats
  ""
  [numdays]
  (let [rawdata (get-n-days-from-today numdays)
        authors (into #{} (map :author rawdata))]
    (pprint authors)))
