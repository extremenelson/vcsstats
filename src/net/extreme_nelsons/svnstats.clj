(ns net.extreme-nelsons.svnstats
  (:require [clojure.java.shell :refer [sh]]
            [clojure.string :refer [split-lines lower-case trim]]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.pprint :as pp]
            [clj-time.format :refer [formatters parse]]
            [clj-time.core :refer [before? date-time]]
            [clojure.string :refer [join]]
            [clj-time.core :refer [today]]
            [net.extreme-nelsons.state :refer
             [update-state get-state get-whole-state]]
            [clojure.data.zip.xml :as zip-xml]
            [clojure.pprint :refer [pprint]]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.walk :refer [postwalk]]
            [incanter.core :as incant]
            [incanter.excel :as excel])
  (:gen-class))

(def svncommands {:svn "svn" :list "list" :log "log" :logxml "--xml" :co "co" :corev "-r"})
(formatters {:bifDate (formatters :date) :bifDateTime (formatters :date-time)})

(defn create-path
  "Creates a path string"
  [path]
  (let [config (get-state :config) {:keys [repo base]} config]
    (join "/" [repo base path])))

(defn check-out-revision
  "Checks out a specific revision of a line of code."
  [path rev]
  (let [{:keys [svn co corev]} svncommands]
    (:out (sh svn co corev rev (create-path path)))))
  
(defn get-log-xml2
  "Gets the log data in xml format for the given path."
  [path]
  (let [{:keys [svn log logxml]} svncommands]
    (:out (sh svn log logxml (create-path path))
  )))

(defn get-log-xml
  [p]
  (slurp "svnxml.out"))

(defn convert-log-to-xml
  "Converts the project log xml to clojure structures"
  [p]
  (xml/parse (java.io.ByteArrayInputStream. (.getBytes (get-log-xml p) "UTF-8"))))

(defn get-log-entries
  "Gets the log entries from the xml structure"
  [proj]
  (:content (first (xml-seq (convert-log-to-xml proj)))))

(defn get-revision-datetime
  "Gets the date timestamp from the xml structure"
  [context struc]
  (let [bifDateTime (:bifDateTime context)]
    (parse bifDateTime (get (:content (get (:content struc) 1)) 0))))

(defn get-revision-id
  "Gets the revision id from the xml structure"
  [struc]
  (:revision (:attrs struc)))

(defn find-revision-by-date
  "Finds the last revision on or before the given date."
  [context proj find-date]
  (let [dt (parse (:bifDate context) find-date)]
    (get-revision-id (first (filter #(before? (get-revision-datetime %) dt) (get-log-entries proj))))))

(defn parse-todays-date
  "Get todays date as a datetime object as of midnight."
  []
  (let [td (today)]
    (date-time (:year td) (:month td) (:day td))))

(defn list-dir
  "Function to get the directory contents."
  [path]
  (println )
  (let [{:keys [svn list]} svncommands]
    (:out (sh svn list (create-path path)))
    )
  )

(defn store-xml
  "Function to store the parsed xml log into the system state for all subsequent functions"
  [path]
  (update-state :log-as-xml (convert-log-to-xml path)))

(defn get-committers
  "Function to get a list of all the committers"
  []
  (let [xml-log (get-state :log-as-xml) committers '()]
    (doseq [x (xml-seq xml-log)
            :when (= :author (:tag x))]
      (cons committers (lower-case (first (:content x))))
      ))
  )

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
  (:content (second (:content entry))))

(defn get-msg
  ""
  [entry]
  (:content (last (:content entry))))

(defn process-logentry
  "Extracts all the information from a logentry"
  [entry]
  (let [author (get-author entry)
        revision (get-revision entry)
        revdate (get-date entry)
        msg (get-msg entry)]
    {:author author :revnum revision :revdate revdate :revmsg msg}))

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

(defn process-repo
  "Process a subversion repository"
  []
  (
   (println "loading repo data")
   (store-xml "")
   (println "processing data")
   (let [start (System/nanoTime)]
     (update-state :processed-data (process-log))
     (print "Took ")
     (print (/ (- (System/nanoTime) start) 1000000000.0))
     (println " seconds")
     )
;   (spit "out.xml" (get-state :processed-data))
   (println "=================")
;(with-open [out-file (io/writer "out.csv")]
                                        ;    (csv/write-csv out-file (postwalk identity (get-state
                                        ;    :processed-data)))
   (write-csv (get-state :processed-data))
   (println "Finished processing")
   ))
