(ns net.extreme-nelsons.svnstats
  (:require [clojure.java.shell :refer [sh]]
            [clojure.string :refer [split-lines lower-case trim]]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clj-time.format :refer [formatters parse parse-local]]
            [clj-time.core :refer [before? date-time today]]
            [net.extreme-nelsons.state :refer
             [update-state get-state get-whole-state]]
            [net.extreme-nelsons.svnserver :refer [get-log-xml2]]
            [clojure.data.zip.xml :as zip-xml]
            [clojure.pprint :refer [pprint]]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.walk :refer [postwalk]]
            [incanter.core :as incant]
            [incanter.excel :as excel])
  )

(def bifDate (formatters :date))
(def bifDateTime (formatters :date-time))

(defn convert-xml-to-struct
  "Converts the project log xml to clojure structures"
  [path]
  (:content (xml/parse (java.io.ByteArrayInputStream. (.getBytes (get-log-xml2 path) "UTF-8")))))

(defn get-log-entries
  "Gets the log entries from the xml structure"
  [proj]
  (:content (first (xml-seq (convert-xml-to-struct proj)))))

(defn get-revision-datetime
  "Gets the date timestamp from the xml structure"
  [struc]
  (parse-local bifDateTime (get (:content (get (:content struc) 1)) 0)))

(defn get-revision-id
  "Gets the revision id from the xml structure"
  [struc]
  (:revision (:attrs struc)))

(defn find-revision-by-date
  "Finds the last revision on or before the given date."
  [context proj find-date]
  (let [dt (parse (:bifDate context) find-date)]
    (get-revision-id (first (filter #(before? (get-revision-datetime %) dt) (get-log-entries proj))))))

(defn process-xml
  "Function to store the parsed xml log into the system state for all subsequent functions"
  [path]
  (update-state :log-as-xml (convert-xml-to-struct path)))

(defn get-committers
  "Function to get a list of all the committers"
  []
  (let [xml-log (get-state :log-as-xml) committers '()]
    (doseq [x (xml-seq xml-log)
            :when (= :author (:tag x))]
      (cons committers (lower-case (first (:content x)))))))

(defn get-author
  "Function to extract the author name from a log entry"
  [entry]
  (lower-case (first (:content (first (:content entry))))))

(defn get-revision
  ""
  [entry]
  (:revision (:attrs entry)))

(defn get-date
  ""
  [entry]
  (parse-local bifDateTime (str (:content (second (:content entry))))))

(defn get-msg
  ""
  [entry]
  (:content (last (:content entry))))

(defn process-file-entry
  ""
  [fentry]
  (let [attrs (:attrs fentry)]
    (conj attrs {:path (:content fentry)})))

(defn process-files
  "Function to process the file paths from a log entry"
  [entry]
  (let [file-info (:content (nth (:content entry) 2 ))]
    (into [] (map process-file-entry file-info))))

(defn process-logentry
  "Extracts all the information from a logentry"
  [entry]
  (let [author (get-author entry)
        revision (get-revision entry)
        revdate (get-date entry)
        msg (get-msg entry)
        fileinfo (process-files entry)]
    {:author author :revnum revision :revdate revdate :revmsg msg :files fileinfo}))

(defn aggregate-all
  ""
  [themap]
  (into {} (for [k (into #{} (mapcat keys themap))
               :let [obj (Object.)]]
           [k (filter (partial not= obj)
                      (map #(get % k obj) themap))])))

(defn process-log
  "Pulls all the needed information from a logentry"
  []
  (let [xml-logs (:content (get-state :log-as-xml)) ]
    (map process-logentry xml-logs)))

(defn write-csv
  ""
  [themap]
  (let [mapset (incant/to-dataset (sort-by :author themap))]
    (excel/save-xls mapset "svnstats.xls")))

(defn testmap
  ""
  [data]
  (pprint (map #(get-revision %) data)))

(defn process-repo
  "Process a subversion repository"
  []
  (process-xml "")
  (println "---------------------")
  (testmap (take 4 (:content (convert-xml-to-struct "")))) 
  (println "---------------------"))
