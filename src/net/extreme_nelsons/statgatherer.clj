(ns net.extreme-nelsons.statgatherer
    (:require [clojure.pprint :as pp]
            [clj-time.format :refer [formatters parse]]
            [clj-time.core :refer [before? date-time today minus- days weeks months within? local-date]]
            [clojure.string :refer [join]]
            [clojure.pprint :refer [pprint]]
            [net.extreme-nelsons.state :refer
             [update-state get-state get-whole-state]]))

;; use frequencies function here

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
  (:revdate entry)
)

(defn get-authors
  ""
  []
  (let [tree (get-state :processed-data)]
    (into #{} (map :author tree))
  )
)

(defn get-revision-list
  ""
  []
  (let [tree (get-state :processed-data)]
    (into [] (map :revnum tree))))

(defn revdate-cond
  ""
  [end start entry]
  (let [thedate (org.joda.time.LocalDate. (:revdate entry))]
    (println (join " " [start end thedate (within? end start thedate)]))
    (within? end start thedate)
  )
)

(defn get-all-in-date-range
  ""
  [start end]
  (let [data (get-state :processed-data)]
    (map #(when-not (= false (revdate-cond end start %1)) %1) (take 10 data))
  ))

(defn get-n-days-from-today
  ""
  [num-days]
  (let [end (minus- (today) (days num-days))]
    (println "end " end)
    (pprint (take 4 (get-all-in-date-range (today) end)))
    (get-all-in-date-range (today) end)))

(defn process-entry
  ""
  [entry]
  )

(defn get-raw-data
  ""
  [numdays]
  (get-n-days-from-today numdays))

(defn get-authors
  "Function to get the set of authors"
  [data]
  (into #{} (map :author data)))

(defn get-author-stats
  ""
  [theauthor data]
  (println "Getting stats for " theauthor)
  (let [author-commits (filter #(= theauthor (:author %))  data)]
    (println (count author-commits))))

(defn get-num-commits
  ""
  [data]
  (count data))

(defn timeperiod-stats
  ""
  [numdays]
  (let [rawdata (get-n-days-from-today numdays)
        authors (get-authors rawdata)
        total-commits (get-num-commits rawdata)]
    (println "Stats following:")
    (pprint (take-last 2 rawdata))
    (pprint authors)
    (pprint total-commits)
    (get-author-stats (first authors) rawdata)))
