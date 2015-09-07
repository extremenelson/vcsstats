(ns net.extreme-nelsons.git
  (:require [clojure.java.shell :refer [sh]]
            [clojure.string :refer [join]]
            [clojure.pprint :refer [pprint]]
            [net.extreme-nelsons.state :refer [get-state]]
            [clj-jgit.porcelain :as porcelain]))

(def therepo (porcelain/load-repo "/home/anelson/git/svnstats/.git"))

(defn get-repo-logs
  "load git repo"
  []
  (porcelain/git-log therepo))

(defn test-log
  ""
  []
  (map #(println (first %)) (get-repo-logs)))
