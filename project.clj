(defproject caro "0.1.0-SNAPSHOT"
  :description "Eval-and-replace, but inside a tree"
  :url "https://github.com/latacora/caro"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [com.rpl/specter "1.1.2"]
                 [cheshire "5.8.1"]
                 [byte-streams "0.2.4"]
                 [com.taoensso/timbre "4.10.0"]]
  :main ^:skip-aot caro.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :native-image {:opts ["-Dclojure.compiler.direct-linking=true"]}}})
