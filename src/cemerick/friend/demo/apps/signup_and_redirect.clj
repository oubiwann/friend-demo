(ns ^{:name "Sign-up and Redirect"
      :doc "Form-based all-in-one sign-up and redirect to authenticated
           space."}
  cemerick.friend.demo.apps.signup-and-redirect
  (:require [cemerick.friend.demo [content :as content]
                                  [roles :as roles]
                                  [users :as users :refer [users]]
                                  [util :as util]]
            [cemerick.friend.demo.content.fragment :as fragment]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [compojure.core :as compojure :refer (GET POST ANY defroutes)]
            (compojure [handler :as handler]
                       [route :as route])
            [ring.util.response :as resp]
            [clojure.string :as str]
            [hiccup.page :as h]
            [hiccup.element :as e]))

(defn- create-user
  [{:keys [username password admin] :as user-data}]
  ;; HERE IS WHERE YOU'D PUSH THE USER INTO YOUR DATABASES if desired
  (-> (dissoc user-data :admin)
      (assoc :identity username
             :password (creds/hash-bcrypt password)
             :roles (into #{roles/user} (when admin [roles/admin])))))

(defroutes routes
  (GET "/" req
    (content/signup-form-page req))
  (GET "/login" req
    (content/interactive-form-page req))
  (POST "/signup" {{:keys [username password confirm]
                    :as params} :params :as req}
        (if (and (not-any? str/blank? [username password confirm])
                 (= password confirm))
          (let [user (create-user
                       (select-keys params [:username :password :admin]))]
            (friend/merge-authentication
              (resp/redirect (util/context-uri req username))
              user))
          (assoc (resp/redirect (str (:context req) "/"))
                 :flash "Passwords don't match!")))
  (GET "/logout" req
    (friend/logout* (resp/redirect (str (:context req) "/"))))
  (GET "/requires-authentication" req
    (friend/authenticated (content/authed-page req)))
  (GET "/role-user" req
    (friend/authorize #{roles/user} (content/user-page req)))
  (GET "/role-admin" req
    (friend/authorize #{roles/admin} (content/admin-page req)))
  (GET "/:user" req
    (friend/authenticated
      (let [user (:user (req :params))]
         (if (= user (:username (friend/current-authentication)))
           (content/signed-up-user-page req user)
           (resp/redirect (str (:context req) "/")))))))

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
