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

(def password-confirm-input
  [:div {:class "form-group"}
   [:label {:for "inputConfirmPassword" :class "col-lg-2 control-label"}
    "Password"]
   [:div {:class "col-lg-10"}
    [:input {:class "form-control" :id "inputConfirmPassword"
             :type "password" :name "confirm"}]]])

(def pin-input
  [:div {:class "form-group"}
   [:label {:for "inputPIN" :class "col-lg-2 control-label"}
    "PIN"]
   [:div {:class "col-lg-10"}
    [:input {:class "form-control" :id "inputPIN" :placeholder "PIN"
             :type "text" :name "pin"}]]])

(def admin?-input
  [:div {:class "form-group"}
   [:label {:for "inputAdmin" :class "col-lg-2 control-label"}
    "Make you an admin?"]
   [:div {:class "col-lg-10"}
    [:input {:class "form-control" :id "inputAdmin"
             :type "checkbox" :name "admin"}]]])

(defn supported-openid-input
  [name url dom-id]
  (html
    [:div {:class "form-group"}
     [:label {:for "inputOpenID" :class "col-lg-2 control-label"}
      [:p {:class "lead"} name]]
     [:div {:class "col-lg-10"}
      [:button {:type "submit" :class "btn btn-primary" :value name}
        "Login"]
      [:input {:class "form-control" :id dom-id
               :placeholder "identifier" :type "hidden" :name "identifier"
               :value url}]]]))

(def custom-openid-input
  [:div {:class "form-group"}
   [:label {:for "inputOpenIDIdentifier" :class "col-lg-2 control-label"}
    "OpenID URL"]
   [:div {:class "col-lg-10"}
    [:input {:class "form-control" :id "inputOpenIDIdentifier"
             :placeholder "https://myname.blogspot.com" :type "text" :name "identifier"}]]])

(defn login-button
  ([]
    (login-button "Login"))
  ([value]
    [:div {:class "form-group"}
     [:div {:class "col-lg-10 col-lg-offset-2"}
      [:button {:type "submit" :class "btn btn-primary" :value value}
        value]]]))

(defn signup-button
  ([]
    (login-button "Sign Up"))
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

(def signup-form
  [:form {:class "form-horizontal" :method "POST" :action "signup"}
   [:fieldset
    [:legend "Login"]
    email-input
    password-input
    password-confirm-input
    admin?-input
    (signup-button)]])

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

(defn get-oauth2-user-status
  [req get-user-handle]
  (html
    [:h3 "Current Status"]
    [:h4 (if-let [identity (friend/identity req)]
           [:div {:class "alert alert-success"}
            [:p {:class "lead"}
              (apply str "Logged in, with these roles: "
                (-> identity
                    friend/current-authentication
                    :roles))]
            [:p {:class "lead"}
              "Logged in as OAuth2 user "
              [:strong (get-user-handle (:current identity))]]
            [:p {:class "lead"}
              "OAuth2 access token " (:current identity)]]
           [:div {:class "alert alert-warning"}
             "Anonymous user"])]))

(defn get-openid-user-status
  [req]
  (html
    [:h3 "Current Status"]
    [:h4 (if-let [identity (friend/identity req)]
           [:div {:class "alert alert-success"}
            [:p {:class "lead"}
              (apply str "Logged in, with these roles: "
                (-> identity
                    friend/current-authentication
                    :roles))]]
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

(defn openid-supported-service-login
  [req providers]
  [:fieldset
    [:legend "Services Logins"]
    (for [{:keys [name url]} providers
          :let [base-login-url (util/context-uri
                                 req (str "/login?identifier=" url))
                dom-id (str (gensym))]]
      [:form {:class "form-horizontal" :method "POST" :action "login"
              :onsubmit (when (.contains ^String url "username")
                          (format "var input = document.getElementById(%s);
                                  input.value = input.value.replace(
                                    'username', prompt(
                                      'What is your %s username?')); return true;"
                            (str \' dom-id \') name))}
      (supported-openid-input name url dom-id)])])

(defn openid-freeform-service-login
  [req]
  [:form {:class "form-horizontal"
          :method "POST"
          :action (util/context-uri req "login")}
   [:fieldset
    [:legend "Freeform Login"]
    custom-openid-input
    (login-button)]])
