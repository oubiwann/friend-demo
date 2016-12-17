(ns cemerick.friend.demo
  (:require [bultitude.core :as b]
            [cemerick.friend.demo [content :as content]
                                  [util :as util]]
            [compojure handler [route :as route]]
            [compojure.core :as compojure :refer [GET defroutes]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.webjars :refer [wrap-webjars]]
            ring.adapter.jetty))

(defn demo-vars
  [ns]
  {:namespace ns
   :ns-name (ns-name ns)
   :name (-> ns meta :name)
   :doc (-> ns meta :doc)
   :route-prefix (util/ns->context ns)
   :app (ns-resolve ns 'app)
   :page (ns-resolve ns 'page)})

(def the-menagerie
  (->> (b/namespaces-on-classpath :prefix util/ns-prefix)
       distinct
       (map #(do (require %) (the-ns %)))
       (map demo-vars)
       (filter #(or (:app %) (:page %)))
       (sort-by :ns-name)))

(defroutes landing
  (GET "/" req (content/landing-page the-menagerie)))

(defn- wrap-app-metadata
  [h app-metadata]
  (fn [req]
    (h (assoc req :demo app-metadata))))

(def site
  (apply compojure/routes
    (wrap-webjars landing)
    (for [{:keys [app page route-prefix] :as metadata} the-menagerie]
         (compojure/context route-prefix []
         (wrap-webjars
           (wrap-app-metadata
             (compojure/routes (or page (fn [_]))
                               (or app (fn [_]))) metadata))))))

(defn run
  []
  (defonce ^:private server
    (ring.adapter.jetty/run-jetty #'site {:port 8080 :join? false}))
  server)

(defn -main
  "For Heroku."
  [& [port]]
  (if port
    (ring.adapter.jetty/run-jetty #'site {:port (Integer. port)})
    (println "No port specified, exiting.")))
