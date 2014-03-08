(ns net.extreme-nelsons.system
  (:require [clj-time.core :refer [today]]
;            [net.extreme-nelsons.lifecycle :refer [start-system stop-system]]
            [clojure.tools.cli :refer [parse-opts]]
            [net.extreme-nelsons.svnstats :refer [process-repo]]
            [net.extreme-nelsons.state :refer [set-state]]
            [net.extreme-nelsons.statgatherer :refer [get-authors get-revision-list get-n-days-from-today timeperiod-stats]]
            [clojure.pprint :refer [pprint]]
            [clojure.string :refer [join]]
))

(def cli-options [
       ["-c" "--config-file" "Config file to use." :default "svnstats.cfg"]
       ["-o" "--output" "File to write the output to." :default "./svnstats.out"]
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
  (->> ["This application collects various statistics from a subversion server."
        ""
        "Usage: svnstats [options] action"
        ""
        "Options:"
        options-summary
        ""
        
        "Actions:"
        "  start    Start a new server"
        "  stop     Stop an existing server"
;        "  status   Print a server's status"
        ""
        "Please refer to the manual page for more information."]
       (clojure.string/join \newline)))

(defn create-config
  "Read in the configuration (Map in a file)"
  [filename]
  (with-open [r (clojure.java.io/reader filename)]
    (read (java.io.PushbackReader. r))))

(defn create-system
  "Creates all necesary items for the system to operate."
  [config]
  {:config config})

(defn dump-entry
  ""
  [entry]
  (println "======== Dumping Entry =====")
  (let [revnum (:revnum entry) revdate (:revdate entry)]
    (println (join " " [revnum revdate])))
  (println "===== End of dump ====="))

(defn start-system
  "Starts the system up."
  [options]
  (println "Starting the system")
  (let [config (create-config (:config-file options))
        sysconfig (into options (create-system config))]
    (set-state sysconfig)
    (process-repo) 
    (println "Testing statistics")
    (pprint (timeperiod-stats 10))
  ))

(defn stop-system
  "Closes the system down."
  [options]
  nil)

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
      "start" (start-system options)
      "stop" (stop-system options)
;      "status" (server/status! options)
;      (exit 1 (usage summary))
      )))
