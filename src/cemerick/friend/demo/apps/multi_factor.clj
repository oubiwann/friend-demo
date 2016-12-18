(ns ^{:name "Multi-factor Authentication"
      :doc "Use multi-factor authentication in a Ring app."}
  cemerick.friend.demo.apps.multi-factor
  (:require [cemerick.friend.demo [content :as content]
                                  [users :refer [users]]
                                  [util :as util]]
            [cemerick.friend :as friend]
            [cemerick.friend.workflows :as workflows]
            [cemerick.friend.credentials :as creds]
            [compojure.core :refer [GET POST routes defroutes]]
            [compojure.handler :as handler]
            [ring.util.response :as resp]
            [hiccup.page :as h]
            [hiccup.element :as e]))

(defn validated?
  [user-record password pin]
  (and user-record
       password
       (creds/bcrypt-verify password (:password user-record))
       (= pin (:pin user-record))))

(defn multi-factor-workflow
  [& {:keys [credential-fn] :as form-config}]
  (routes
    (GET "/login" req
      (content/multifactor-login-page req))
    (GET "/logout" req
      (friend/logout* (resp/redirect (str (:context req) "/"))))
    (POST "/start" {:keys [params session] :as req}
      (if-let [user-record (-> params :username credential-fn)]
        (-> (content/pin-page req user-record false)
            resp/response
            (assoc :session session)
            (update-in [:session] assoc :user-record user-record))
        (resp/redirect (util/context-uri req "login"))))
    (POST "/finish" {{:keys [password pin]} :params
                     {:keys [user-record]} :session
                     :as req}
       (if (validated? user-record password pin)
         (workflows/make-auth (dissoc user-record :password)
           {::friend/workflow :multi-factor
            ::friend/redirect-on-auth? true})
         (content/pin-page req user-record true)))
    ; (GET "/role-user" req
    ;   (friend/authorize #{::users/user} (content/user-page req)))
    ; (GET "/role-admin" req
    ;   (friend/authorize #{::users/admin} (content/admin-page req)))
    ))

(defroutes app*
  (GET "/requires-authentication" req
    (friend/authenticated (content/authed-page req))))

(def secured-app
  (friend/authenticate
    app*
    {:allow-anon? true
     :login-uri "/login"
     :workflows [(multi-factor-workflow :credential-fn @users)]}))

(def app (handler/site secured-app))

(defroutes page
  (GET "/" req
    (content/multifactor-page req)))
