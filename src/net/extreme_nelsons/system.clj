(ns system (:require [clj-time.core :refer [today]]
                      [net.extreme-nelsons.svnstats.lifecycle :refer [start-system stop-system]]))

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
        "Usage: svnstats [options] action"
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

(defn create-config
  "Read in the configuration (Map in a file)"
  [filename]
  (with-open [r (clojure.java.io/reader filename)]
    (read (java.io.PushbackReader. r))))

(defn create-system [cfg]
  {:config cfg})

;(defn create-system [cfg]
;  {:jetty (create-jetty-server cfg)
;   :rabbit (create-rabbitmq-channel cfg)
;   :order [:rabbit :jetty])

(defn app-main []
  (let [cfg (create-config)
        system (create-system cfg)]
    (start-system system)
    ;; (.await) / (.join) / (deref) etc...
    (stop-system system)))
