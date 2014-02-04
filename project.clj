(defproject svn_stats "0.1.0-SNAPSHOT"
  :description "Web application to provide a variety of statistics from a Subversion server."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/data.zip "0.1.1"]
                 [org.clojure/tools.cli "0.3.1"]
                 [clj-time "0.6.0"]
                 [ring-server "0.3.1"]]
  :main svn_stats.core)
