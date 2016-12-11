(defproject com.cemerick/friend-demo "0.1.0-SNAPSHOT"
  :description "(eventually,) An über-demo of all that Friend has to offer."
  :url "http://github.com/cemerick/friend-demo"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :resource-paths ["resources"]
  :source-paths ["src/clj"]
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.cemerick/friend "0.2.3"]
                 [compojure "1.6.0-beta2"]
                 [clj-http "3.4.1"]
                 [clojusc/twig "0.3.1-SNAPSHOT"]
                 [com.cemerick/url "0.1.1"]
                 ;; only used for the oauth-related demos
                 [clojusc/friend-oauth2 "0.1.3"]
                 ;; only used to generate demo app pages
                 [hiccup "1.0.5"]
                 ;; only used to discover demo app namespaces
                 [bultitude "0.2.8"]
                 ;; only used for foundation js/css
                 [org.webjars/foundation "6.2.3"]]
  ;; the final clean keeps AOT garbage out of the REPL's way, and keeps
  ;; the namespace metadata available at runtime
  :aliases  {"sanity-check" ["do" "clean," "compile" ":all," "clean"]}
  :main cemerick.friend-demo
  :ring {:handler cemerick.friend-demo/site
         :init cemerick.friend-demo/init})
