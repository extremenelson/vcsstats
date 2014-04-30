(ns net.extreme-nelsons.svnserver
  (:require [clojure.java.shell :refer [sh]]
            [clojure.string :refer [join]]
            [clojure.pprint :refer [pprint]]
            [net.extreme-nelsons.state :refer [get-state]])
  (:gen-class))

(def svncommands {:svn "svn" :list "list" :log "log" :verbose "-v" :logxml "--xml" :co "co" :corev "-r"})

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

(defn list-dir
  "Function to get the directory contents."
  [path]
  (let [{:keys [svn list]} svncommands]
    (:out (sh svn list (create-path path)))))

(defn get-log-xml
  "Gets the log data in xml format for the given path."
  [path]
  (let [{:keys [svn log verbose logxml]} svncommands]
    (:out (sh svn log verbose logxml (create-path path))
  )))

(defn get-log-xml2
  "fake input"
  [path]
  (slurp "xmllog.out"))
