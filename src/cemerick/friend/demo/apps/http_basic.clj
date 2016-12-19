(ns ^{:name "HTTP Basic"
      :doc "Use HTTP Basic to authenticate to a Ring app."}
  cemerick.friend.demo.apps.http-basic
  (:require [cemerick.friend.demo [content :as content]
                                  [roles :as roles]
                                  [users :refer [users]]
                                  [util :as util]]
            [cemerick.friend.demo.content.fragment :as fragment]
            [cemerick.friend :as friend]
            [cemerick.friend [workflows :as workflows]
                             [credentials :as creds]]
            [compojure.core :refer [GET defroutes]]
            [compojure.handler :as handler]
            [hiccup.page :as h]
            [hiccup.element :as element]
            [ring.util.response :as resp]))

(def realm "Friend Demo")

(defroutes routes
  (GET "/" req
    (content/http-basic-page
      req
      (fragment/http-basic-footer req)))
  (GET "/logout" req
    (friend/logout* (resp/redirect (str (:context req) "/"))))
  (GET "/requires-authentication" req
    (friend/authenticated (content/authed-page req)))
  (GET "/role-user" req
    (friend/authorize #{roles/user} (content/user-page req)))
  (GET "/role-admin" req
    (friend/authorize #{roles/admin} (content/admin-page req))))

(def auth-opts
  {:allow-anon? true
   :unauthorized-handler #(-> (content/unauthed-page % (:uri %))
                              resp/response
                              (resp/status 401))
   :workflows [(workflows/http-basic
                :credential-fn #(creds/bcrypt-credential-fn @users %)
                :realm realm)]})

(def app
  (-> routes
      (friend/authenticate auth-opts)
      (handler/site)))
