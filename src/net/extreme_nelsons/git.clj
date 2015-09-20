(ns net.extreme-nelsons.git
  (:require [clojure.java.shell :refer [sh]]
            [clojure.string :refer [join]]
            [clojure.pprint :refer [pprint]]
            [net.extreme-nelsons.state :refer [get-state]]
            [clj-jgit.porcelain :as git]
            [clj-jgit.querying :as gitquery]
            [clj-jgit.internal :as gitinternal]
            [clj-time.coerce :as c]))

;;(def therepo (porcelain/load-repo "/home/anelson/git/vcsstats/.git"))

(def repo-path "/home/anelson/git/vcsstats/.git")

(defn load-repo
  "Load in a repo from some source."
  [repo-path]
  (git/load-repo repo-path))

(defn get-repo-logs
  "load git repo"
  [repo-path]
  (git/git-log (load-repo repo-path)))

(defn convert-time-info
  "Converts the time portion of a JGit PersonIdent to a map"
  [author-ident]
  (hash-map :timestamp (c/from-date (.getWhen author-ident))))
  
(defn convert-author-info
  "Converts author information from a JGit PersonIdent to a map"
  [author-ident]
  (hash-map :name (.getName author-ident),
     :email (.getEmailAddress author-ident),
     :timezone (.getTimeZone author-ident)))

(defn convert-author-ident
  "Converts author info from a JGit AuthorIdent object into maps"
  [author-ident]
  (hash-map :author (convert-author-info author-ident)))

(defn convert-message
  "Extracts the commit message"
  [log-entry]
  (hash-map :logmessage (.getFullMessage log-entry)))

(defn get-author-ident
  "Gets the AuthorIdent object from a LogEntry"
  [log-entry]
  (.getAuthorIdent log-entry))
   
(defn convert-log-entry
  "Converts a JGit RevCommit to a map structure"
  [log-entry]
  (let [ident (get-author-ident log-entry),
        author (convert-author-ident ident)
        message (convert-message log-entry)
        timestamp (convert-time-info ident)]
    (into {} [author message timestamp])))

(defn get-repo
  ""
  [path]
  (git/load-repo path))
   
(defn test-log
  ""
  []
  (let [repo-path "/home/anelson/git/vcsstats/.git"]
    (map #(convert-log-entry %) (get-repo-logs repo-path))))

(defn get-repo-history
  ""
  [repo]
  (let [rev-walk (gitinternal/new-rev-walk repo)
      commit-map (gitquery/build-commit-map repo rev-walk)
      commits (git/git-log repo)]
    (map (partial gitquery/commit-info repo rev-walk commit-map) commits)))

(defn test4
  ""
  []
  (let [repo (get-repo "/home/anelson/git/vcsstats/.git")
        history (get-repo-history repo)]
    (map #(clojure.pprint/pprint %) history)))
