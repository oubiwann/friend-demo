(ns ^{:name "Interactive Form Authentication"
      :doc "Typical username/password authentication + logout + a pinch of
           authorization functionality."}
  cemerick.friend.demo.apps.interactive-form
  (:require [cemerick.friend.demo [content :as content]
                                  [users :as users :refer [users]]
                                  [util :as util]]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [compojure.core :as compojure :refer [GET POST ANY defroutes]]
            [compojure [handler :as handler]
                       [route :as route]]
            [ring.util.response :as resp]
            [hiccup.page :as h]))

(defroutes routes
  (GET "/" req
    (content/interactive-form-page req))
  (GET "/login" req
    (h/html5 content/head (content/body content/login-form)))
  (GET "/logout" req
    (friend/logout* (resp/redirect (str (:context req) "/"))))
  (GET "/requires-authentication" req
    (friend/authenticated (content/authed-page req)))
  (GET "/role-user" req
    (friend/authorize #{::users/user} (content/user-page req)))
  (GET "/role-admin" req
    (friend/authorize #{::users/admin} (content/admin-page req))))

(def app
  (handler/site
    (friend/authenticate
      routes
      {:allow-anon? true
       :login-uri "/login"
       :default-landing-uri "/"
       :unauthorized-handler #(-> (content/unauthed-page % (:uri %))
                                  resp/response
                                  (resp/status 401))
       :credential-fn #(creds/bcrypt-credential-fn @users %)
       :workflows [(workflows/interactive-form)]})))
