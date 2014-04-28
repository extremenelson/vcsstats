(ns net.extreme-nelsons.svnstats
  (:require [clojure.java.shell :refer [sh]]
            [clojure.string :refer [split-lines lower-case trim]]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.pprint :as pp]
            [clj-time.format :refer [formatters parse parse-local]]
            [clj-time.core :refer [before? date-time today]]
            [clojure.string :refer [join]]
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

(def svncommands {:svn "svn" :list "list" :log "log" :verbose "-v" :logxml "--xml" :co "co" :corev "-r"})
(def bifDate (formatters :date))
(def bifDateTime (formatters :date-time))

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
  (let [{:keys [svn log verbose logxml]} svncommands]
    (:out (sh svn log verbose logxml (create-path path))
  )))

(defn get-log-xml2
  "fake input"
  [p]
  (slurp "xmllog.out"))
