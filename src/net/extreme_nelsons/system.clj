(ns net.extreme-nelsons.system
  (:require [clj-time.core :refer [today]]
;            [net.extreme-nelsons.lifecycle :refer [start-system stop-system]]
            [clojure.tools.cli :refer [parse-opts]]))

(def state (atom {}))

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

(defn get-system
  [key]
  (@state key))

(defn update-state [key val]
  (swap! state assoc key val))

(defn set-state [newsystem]
  (println newsystem)
  (reset! state newsystem))

(defn create-config
  "Read in the configuration (Map in a file)"
  [filename]
  (with-open [r (clojure.java.io/reader filename)]
    (read (java.io.PushbackReader. r))))

(defn create-system
  "Creates all necesary items for the system to operate."
  [config]
  {:config config})

(defn start-system
  "Starts the system up."
  [options]
  (let [config (create-config (:config-file options))
        sysconfig (into options (create-system config))]
    (set-state sysconfig)
  ))

(defn stop-system
  "Closes the system down."
  [options]
  nil)

;(defn create-system [cfg]
;  {:jetty (create-jetty-server cfg)
;   :rabbit (create-rabbitmq-channel cfg)
;   :order [:rabbit :jetty])

;(defn app-main []
;(let [cfg (create-config)
;      system (create-system cfg)]
;  (start-system system)
  ;; (.await) / (.join) / (deref) etc...
                                        ;  (stop-system system)))

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
