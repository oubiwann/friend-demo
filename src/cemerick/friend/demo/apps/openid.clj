(ns ^{:name "OpenID"
      :doc "Using OpenID to authenticate with various services: Yahoo, AOL,
            Wordpress, Ubuntu"}
  cemerick.friend.demo.apps.openid
  (:require [cemerick.friend.demo [content :as content]
                                  [util :as util]]
            [cemerick.friend.demo.content.fragment :as fragment]
            [cemerick.friend :as friend]
            [cemerick.friend.openid :as openid]
            [compojure.core :refer (GET defroutes)]
            (compojure [handler :as handler])
            [ring.util.response :as resp]
            [hiccup.page :as h]))

(def providers [{:name "Yahoo" :url "http://me.yahoo.com/"}
                {:name "AOL" :url "http://openid.aol.com/"}
                {:name "Wordpress.com" :url "http://username.wordpress.com"}
                {:name "Ubuntu" :url "https://login.launchpad.net/+openid"}])

(defroutes routes
  (GET "/" req
    (content/openid-page req providers))
  (GET "/logout" req
    (friend/logout* (resp/redirect (str (:context req) "/")))))

(def auth-opts
  {:allow-anon? true
   :default-landing-uri "/"
   :workflows [(openid/workflow
                 :openid-uri "/login"
                 :credential-fn identity)]})

(def app
  (-> routes
      (friend/authenticate auth-opts)
      (handler/site)))
