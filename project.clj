(defproject svn_stats "0.1.0-SNAPSHOT"
  :description "Application to provide a variety of statistics from a Subversion server."
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.zip "0.1.1"]
                 [org.clojure/tools.cli "0.3.1"]
                 [clj-time "0.6.0"]
                 [org.clojure/data.csv "0.1.2"]
                 [incanter "1.5.4"]
;                 [ring-server "0.3.1"]
                 ]
  :main net.extreme-nelsons.system
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "0.2.4"]
                                  [org.clojure/java.classpath "0.2.2"]]}
             :uberjar {:aot [net.extreme-nelsons.system]}}
  :uberjar-name "svnstats.jar")
