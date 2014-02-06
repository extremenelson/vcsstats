(ns net.extreme-nelsons.svnstats (:require [clojure.java.shell :refer [sh]]
            [clojure.string :refer [split-lines]]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.pprint :as pp]
            [clj-time.format :refer [formatters parse]]
            [clj-time.core :refer [before? date-time]]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(def prod-context)
(def dev-context {:svn-cmd "svn" :svn-list "list" :svn-log "log" :svn-log-xml "--xml"
                  :bifDate (formatters :date) :bifDateTime (formatters :date-time)})


(defn check-out-revision
  "Checks out a specific revision of a line of code."
  [context location revision]
  (let [svn (:svn context)]
    (:out (sh svn "co" "-r" revision location))))
  
(defn get-project-log-xml
  "Gets the log data for the given project."
  [context svnloc p]
  (let [svn (:svn context) svn-log (:svn-log context) svn-log-xml (:svn-log-xml context)]
    (:out (sh svn svn-log svn-log-xml (str svnloc p)))))

(defn convert-log-to-struct
  "Converts the project log xml to clojure structures"
  [p]
  (xml/parse (java.io.ByteArrayInputStream. (.getBytes (get-project-log-xml p) "UTF-8"))))

(defn get-log-entries
  "Gets the log entries from the xml structure"
  [proj]
  (:content (first (xml-seq (convert-log-to-struct proj)))))

(defn svn-list-dir
  "Returns the content of the directory in Subversion"
  [options repo path]
  (let [svn (:svn options) svn-list (:svn-list options)]
  (:out (sh svn svn-list path))))

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

((defn -main
  "Application entry point"
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    ;; Handle help and error conditions
    (cond
      (:help options) (exit 0 (usage summary))
      (not= (count arguments) 1) (exit 1 (usage summary))
      errors (exit 1 (error-msg errors)))
    ;; Execute program with options
    (case (first arguments)
      "start" (server/start! options)
      "stop" (server/stop! options)
      "status" (server/status! options)
      (exit 1 (usage summary))))))
