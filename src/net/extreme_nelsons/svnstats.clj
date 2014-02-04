(ns net.extreme-nelsons.svnstats (:require [clojure.java.shell :refer [sh]]
            [clojure.string :refer [split-lines]]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.pprint :as pp]
            [clj-time.format :refer [formatters parse]]
            [clj-time.core :refer [before? today date-time]]
            [clojure.tools.cli :refer [parse-opts]])
  (:gen-class))

(def prod-context)
(def dev-context {:svn-cmd "svn" :svn-list "list" :svn-log "log" :svn-log-xml "--xml"
                  :bifDate (formatters :date) :bifDateTime (formatters :date-time)})

(def cli-options [
       ["-c" "--config " "Config file to use." :default "mma-loc.cfg"]
       ["-o" "--output" "File to write the output to." :default "./svn_stats.out"]
       ["-h" "--help" "Show help" :default false :flag true]
       ["-d" "--date" "Date to use for checking out the code." :default (today)]
       ])

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (clojure.string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn usage [options-summary]
  (->> ["This is my program. There are many like it, but this one is mine."
        ""
        "Usage: svn_dev_info [options] action"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  start    Start a new server"
        "  stop     Stop an existing server"
        "  status   Print a server's status"
        ""
        "Please refer to the manual page for more information."]
       (clojure.string/join \newline)))

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

(defn read-config-file
  "Reads the config file from the system."
  [options]
  (slurp (:config options)))

(defn load-config
  "Read in the configuration (Map in a file)"
  [filename]
  (with-open [r (clojure.java.io/reader filename)]
    (read (java.io.PushbackReader. r))))

(defn system
  "This is the main processing function for the application."
  [options context]
  (print (load-config (:config options))))

(defn -main
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
      (exit 1 (usage summary)))))
