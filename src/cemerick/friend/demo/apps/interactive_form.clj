(ns ^{:name "Interactive Form Authentication"
      :doc "Typical username/password authentication + logout + a pinch of
           authorization functionality."}
  cemerick.friend.demo.apps.interactive-form
  (:require [cemerick.friend.demo [content :as content]
                                  [roles :as roles]
                                  [users :as users :refer [users]]
                                  [util :as util]]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [compojure.core :as compojure :refer [GET POST ANY defroutes]]
            [compojure.handler :as handler]
            [ring.util.response :as resp]
            [hiccup.page :as page]))

(defroutes routes
  (GET "/" req
    (content/interactive-form-page req))
  (GET "/login" req
    (content/login-page req))
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
   :login-uri "/login"
   :default-landing-uri "/"
   :unauthorized-handler #(-> (content/unauthed-page % (:uri %))
                              resp/response
                              (resp/status 401))
   :credential-fn #(creds/bcrypt-credential-fn @users %)
   :workflows [(workflows/interactive-form)]})

(def app
  (-> routes
      (friend/authenticate auth-opts)
      (handler/site)))
