(ns net.extreme-nelsons.svnstats
  (:require [clojure.java.shell :refer [sh]]
            [clojure.string :refer [split-lines]]
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
            [clojure.pprint :refer [pprint]])
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
  
(defn get-log-xml
  "Gets the log data in xml format for the given path."
  [path]
  (let [{:keys [svn log logxml]} svncommands]
    (:out (sh svn log logxml (create-path path))
  )))

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

(defn process-repo
  "Process a subversion repository"
  []
  (
   (println "processing repo")
   (let [parsed-xml (zip/xml-zip (convert-log-to-xml ""))]
     (println "got output")
     (spit "out.xml" parsed-xml)
     (println (zip-xml/xml-> :logentry))
     (println "Finished processing")
     )))
