(ns ^{:name "GitHub using OAuth2"
      :doc "Authenticating via GitHub using OAuth2 [EXPERIMENTAL]."}
  cemerick.friend.demo.apps.oauth-github
  (:require [cemerick.friend.demo [content :as content]
                                  [roles :as roles]
                                  [users :as users :refer [users]]
                                  [util :as util]]
            [cemerick.friend.demo.content.fragment :as fragment]
            [cemerick.friend :as friend]
            [cemerick.friend [workflows :as workflows]
                             [credentials :as creds]]
            [cemerick.url :as url]
            [friend-oauth2.workflow :as oauth2]
            [friend-oauth2.util :as oauth2-util]
            [clj-http.client :as http]
            [cheshire.core :as json]
            [compojure.core :as compojure :refer (GET defroutes)]
            (compojure [handler :as handler]
                       [route :as route])
            [ring.util.response :as resp]
            [ring.util.codec :as codec]
            [hiccup.page :as h]
            [hiccup.element :as e]))

(def client-id (System/getenv "OAUTH2_CLIENT_ID"))
(def client-secret (System/getenv "OAUTH2_CLIENT_SECRET"))
(def callback-domain (System/getenv "OAUTH2_CALLBACK_DOMAIN"))
(def callback-path-segment "callback")
(def full-callback-path (str "/oauth-github/" callback-path-segment))

(defn- call-github
  [endpoint access-token]
  (-> (format "https://api.github.com%s%s&access_token=%s"
        endpoint
        (when-not (.contains endpoint "?") "?")
        access-token)
    http/get
    :body
    (json/parse-string (fn [^String s] (keyword (.replace s \_ \-))))))

;; This sort of blind memoization is *bad*. Please don't do this in your real apps.
;; Go use an appropriate cache from https://github.com/clojure/core.cache
(def get-public-repos (memoize (partial call-github "/user/repos?type=public")))
(def get-github-handle (memoize (comp :login (partial call-github "/user"))))
(def get-access-token #(-> % :body codec/form-decode (get "access_token")))

(defroutes routes
  (GET "/" req
    (content/github-oauth2-page req get-github-handle get-public-repos))
  (GET "/login" req
    (content/login-page req))
  (GET "/logout" req
    (friend/logout* (resp/redirect (str (:context req) "/"))))
  (GET "/requires-authentication" req
    (friend/authenticated (content/authed-page req)))
  (GET "/role-user" req
    (friend/authorize #{roles/user} (content/user-page req)))
  #_(GET "/role-admin" req
    (friend/authorize #{roles/admin} (content/admin-page req))))

(def client-config
  {:client-id client-id
   :client-secret client-secret
   ;; TODO get friend-oauth2 to support :context, :path-info
   :callback {:domain callback-domain
              :path full-callback-path}})

(def uri-config
  {:authentication-uri {:url "https://github.com/login/oauth/authorize"
                        :query {:client_id (:client-id client-config)
                                :response_type "code"
                                :redirect_uri (oauth2-util/format-config-uri client-config)
                                :scope ""}}

   :access-token-uri {:url "https://github.com/login/oauth/access_token"
                      :query {:client_id (:client-id client-config)
                              :client_secret (:client-secret client-config)
                              :grant_type "authorization_code"
                              :redirect_uri (oauth2-util/format-config-uri client-config)
                              :code ""}}})

(def auth-opts
  {:allow-anon? true
   :default-landing-uri "/"
   :login-uri (str "/" callback-path-segment)
   :unauthorized-handler #(-> (content/unauthed-page % (:uri %))
                              resp/response
                              (resp/status 401))
   :workflows [(oauth2/workflow
                 {:client-config client-config
                  :uri-config uri-config
                  :config-auth {:roles #{roles/user}}
                  :access-token-parsefn get-access-token})]})

(def app
  (-> routes
      (friend/authenticate auth-opts)
      (handler/site)))
