(ns ^{:name "HTTP Basic"
      :doc "Use HTTP Basic to authenticate to a Ring app."}
  cemerick.friend.demo.apps.http-basic
  (:require [cemerick.friend.demo [content :as content]
                                  [users :refer [users]]
                                  [util :as util]]
            [cemerick.friend :as friend]
            [cemerick.friend [workflows :as workflows]
                             [credentials :as creds]]
            [compojure.core :refer [GET defroutes]]
            [compojure.handler :refer [site]]
            [hiccup.page :as h]
            [hiccup.element :as element]
            [clojure.string :as str]))

(defroutes app*
  (GET "/requires-authentication" req
       (friend/authenticated (str "You have successfully authenticated as "
                                  (friend/current-authentication)))))

(def secured-app (friend/authenticate
                   app*
                   {:allow-anon? true
                    :unauthenticated-handler #(workflows/http-basic-deny "Friend demo" %)
                    :workflows [(workflows/http-basic
                                 :credential-fn #(creds/bcrypt-credential-fn @users %)
                                 :realm "Friend demo")]}))

(def app (site secured-app))

(defroutes page
  (GET "/" req
    (content/http-basic-page
      req
      (content/http-basic-footer req))))
