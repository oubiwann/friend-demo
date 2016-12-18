(ns cemerick.friend.demo.content.fragment
  (:require [cemerick.friend :as friend]
            [cemerick.friend.demo.util :as util]
            [hiccup.core :refer [html]]
            [hiccup.element :as element]))

(defn body
  [& content]
  [:body
   [:div {:class "container"}
    [:div {:class "row"}
    (into [:div {:class "columns small-12"}] content)]]])

(def head
  [:head [:link {:href "assets/bootswatch-superhero/css/bootstrap.min.css"
                 :rel "stylesheet"
                 :type "text/css"}]])

(defn jumbotron
  []
  [:div {:class "jumbotron"}
   [:h1 "cemerick.friend.demo"]
   [:p "... a collection of demonstration apps using "
       (element/link-to "http://github.com/cemerick/friend" "Friend")
       ", an authentication and authorization library for securing Clojure web "
       "services and applications."]])

(defn github-link
  [req]
  [:div {:style "float:right; width:50%; margin-top:1em; text-align:right"}
   [:a {:class "button" :href (util/github-url-for (-> req :demo :ns-name))}
       "View source"]
   " | "
   [:a {:class "button secondary" :href "/"} "All demos"]])

(defn http-basic-footer
  [req]
  [:p "You can combine this with Friend's \"channel security\" middleware to "
      "enforce the use of SSL, making this a good recipe for controlling "
      "access to web service APIs. Head over to "
      (element/link-to (util/context-uri req "/https-basic") "HTTPS Basic")
      " for a demo."])

(defn https-basic-footer
  [req]
  [:p "Note that because the handler that requires authentication is further "
      "guarded by "
      [:code "cemerick.friend/requires-scheme"]
      ", all requests to it are redirected over HTTPS (even before the HTTP "
      "Basic challenge is sent, if required)."])

(def email-input
  [:div {:class "form-group"}
   [:label {:for "inputEmail" :class "col-lg-2 control-label"}
     "Email"]
   [:div {:class "col-lg-10"}
    [:input {:class "form-control" :id "inputEmail" :placeholder "Email"
             :type "text" :name "username"}]]])

(def password-input
  [:div {:class "form-group"}
   [:label {:for "inputPassword" :class "col-lg-2 control-label"}
    "Password"]
   [:div {:class "col-lg-10"}
    [:input {:class "form-control" :id "inputPassword" :placeholder "Password"
             :type "password" :name "password"}]]])

(def pin-input
  [:div {:class "form-group"}
   [:label {:for "inputPIN" :class "col-lg-2 control-label"}
    "PIN"]
   [:div {:class "col-lg-10"}
    [:input {:class "form-control" :id "inputPIN" :placeholder "PIN"
             :type "text" :name "pin"}]]])

(defn login-button
  ([]
    (login-button "Login"))
  ([value]
    [:div {:class "form-group"}
     [:div {:class "col-lg-10 col-lg-offset-2"}
      [:button {:type "submit" :class "btn btn-primary" :value value}
        value]]]))

(def login-form
  [:form {:class "form-horizontal" :method "POST" :action "login"}
   [:fieldset
    [:legend "Login"]
    email-input
    password-input
    (login-button)]])

(defn logging-out-section
  [req]
  (html
    [:h3 "Logging out"]
     [:p "Click below to log out."]
     [:p [:a {:class "btn btn-primary"
              :href (util/context-uri req "logout")}
           "Logout"]]))

(defn get-user-status
  [req]
  (html
    [:h3 "Current Status"]
    [:h4 (if-let [identity (friend/identity req)]
         [:div {:class "alert alert-success"}
          (apply str "Logged in, with these roles: "
            (-> identity
                friend/current-authentication
                :roles))]
         [:div {:class "alert alert-warning"}
           "Anonymous user"])]))

(defn get-protected-links
  [req]
  (html
    [:h3 "Authorization demos"]
    [:p "Each of these links require particular roles (or, any "
        "authentication) to access. If you're not authenticated, you will be "
        "redirected to a dedicated login page. If you're already "
        "authenticated, but do not meet the authorization requirements (e.g. "
        "you don't have the proper role), then you'll get an Unauthorized "
        "HTTP response."]
    [:ul
     [:li (element/link-to
            (util/context-uri req "role-user")
            "Requires the `user` role")]
     [:li (element/link-to
            (util/context-uri req "role-admin")
            "Requires the `admin` role")]
     [:li (element/link-to
            (util/context-uri req "requires-authentication")
            "Requires any authentication, no specific role requirement")]]))

(def multifactor-finish-form
  [:form {:class "form-horizontal" :method "POST" :action "finish"}
   [:fieldset
    [:legend "Login"]
    password-input
    pin-input
    (login-button "Verify PIN")]])
