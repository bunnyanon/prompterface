(defproject prompterface "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [ring/ring "1.12.2"]
                 [compojure "1.7.1"]
                 [hiccup "2.0.0-RC3"]
                 [org.clojure/data.json "2.5.0"]
                 [org.xerial/sqlite-jdbc "3.23.1"]
                 [org.clojure/java.jdbc "0.7.12"]
                 [metosin/malli "0.16.4"]]
  :main ^:skip-aot prompterface.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
