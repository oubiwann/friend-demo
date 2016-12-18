(ns ^{:name "HTTP Basic requiring SSL"
      :doc "Same as 'HTTP Basic', but with the added condition that HTTPS/SSL
            is used (suitable for web service APIs). Requires that you have
            the app running on port 443 for SSL traffice."}
  cemerick.friend.demo.apps.https-basic
  (:require [cemerick.friend :as friend]
            [cemerick.friend.demo.apps.http-basic :as basic]
            [cemerick.friend.demo.content :as content]
            [cemerick.friend.demo.content.fragment :as fragment]
            [compojure.core :refer [GET]]
            [compojure.handler :as handler]))

(def app
  "This app expects that requests will be accessible via HTTPS (port 443).

  If you don't have this app running on the secure port, the page for this
  demo will not load in the browser."
  (-> basic/routes
      (friend/authenticate basic/auth-opts)
      (friend/requires-scheme-with-proxy :https)
      (handler/site)))
