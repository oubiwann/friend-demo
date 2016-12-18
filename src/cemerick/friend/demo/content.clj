(ns cemerick.friend.demo.content
  (:require [clojure.string :as str]
            [cemerick.friend :as friend]
            [cemerick.friend.demo.users :refer [users]]
            [cemerick.friend.demo.util :as util]
            [hiccup.core]
            [hiccup.element :as element]
            [hiccup.page]))

;;; HTML Fraagments

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
  (hiccup.core/html
    [:h3 "Logging out"]
     [:p "Click below to log out."]
     [:p [:a {:class "btn btn-primary"
              :href (util/context-uri req "logout")}
           "Logout"]]))

(defn get-user-status
  [req]
  (hiccup.core/html
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
  (hiccup.core/html
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

;;; Sub-pages for apps

(defn user-page
  [req]
  (hiccup.page/html5
    head
    (body
     [:h2 (-> req :demo :name)]
     [:p {:class "lead"} "You're a user!"])))

(defn admin-page
  [req]
  (hiccup.page/html5
    head
    (body
     [:h2 (-> req :demo :name)]
     [:p {:class "lead"} "You're an admin!"])))

(defn authed-page
  [req]
  (hiccup.page/html5
    head
    (body
     [:h2 (-> req :demo :name)]
     [:p {:class "lead"} "Thanks for authenticating!"])))

(defn unauthed-page
  [req uri]
  (hiccup.page/html5
    head
    (body
     [:h2 (-> req :demo :name)]
     [:div {:class "alert alert-danger"}
     [:p {:class "lead"}
       "You do not have sufficient privileges to access "
       uri]])))

(defn multifactor-login-page
  [req]
  (hiccup.page/html5
    head
    (body
    [:h2 (-> req :demo :name)]
    [:form {:class "form-horizontal" :method "POST" :action "start"}
     [:fieldset
      [:legend "Login"]
      email-input
      (login-button)]])))

(defn pin-page
  [req identity invalid-login?]
  (hiccup.page/html5
    head
    (body
      [:h2 (-> req :demo :name)]
      (when invalid-login?
        [:div {:class "alert alert-danger"}
         [:p {:class "lead"} "Sorry, that's not correct!"]])
      [:div {:class "alert alert-success"}
       [:p {:class "lead"}
        "Hello, " (:username identity) "; it looks like you're a "
        (-> identity :roles first name) "."]]
      [:p {:class "lead"}
        "We've sent you a PIN ("
        [:em "not really; it's always `1234` in this demo,
          but you could use something like Twilio to send a PIN"]
        ")."]
      [:p {:class "lead"}
        "Please enter it and your password here:"]
      multifactor-finish-form)))

;;; Top-level and App Pages

(defn landing-page
  [apps]
  (hiccup.core/html
    [:html
      head
      (body
       [:h1
        [:a {:href "http://github.com/cemerick/friend-demo"} "Among Friends"]]
       (jumbotron)
       [:p {:class "lead"}
           "Implementing authentication and authorization for your web apps is
           generally a necessary but not particularly pleasant task, even if
           you are using Clojure. Friend makes it relatively easy and
           relatively painless, but I thought the examples that the project's
           documentation demanded deserved a better forum than to bit-rot in a
           markdown file or somesuch."]
       [:p
           "So, what better than a bunch of live
           demos of each authentication workflow that Friend supports (or is
           available via another library that builds on top of Friend), with
           smatterings of authorization examples here and there, all with links
           to the generally-less-than-10-lines of code that makes it happen?"]
       [:p
           "Check out the demos, find the one(s) that apply to your situation,
           and click the button on the right to go straight to the source for
           that demo:"]
       [:div {:class "columns small-8"}
        [:h2 "Demonstrations"]
        [:ol
         (for [{:keys [name doc route-prefix]} apps]
           [:li
            [:p {:class "lead"}
              (element/link-to (str route-prefix "/") [:strong name])
            " — " doc]])]]
       [:div {:class "columns small-4"}
        [:h2 "Credentials"]
        [:p "All demo applications here that directly require user-provided
            credentials recognize two different username/password combinations:"]
        [:ul [:li [:code "friend/clojure"]
                  " — associated with a \"user\" role"]
             [:li [:code "friend-admin/clojure"]
                  " — associated with an \"admin\" role"]]])]))

(defn http-basic-page
  [req footer]
  (hiccup.page/html5
    head
    (body
     (github-link req)
     [:h2 (-> req :demo :name)]
     [:p {:class "lead"}
         "Attempting to access "
         (element/link-to {:id "interactive_url"}
           (util/context-uri req "requires-authentication") "this link ")
         "will issue a challenge for your user-agent (browser) to provide "
         "HTTP Basic credentials. Once authenticated, all the authorization "
         "options available in Friend are available to restrict the "
         "permissions of particular users."]
     [:p "Please note that Chrome (and maybe other browsers) silently save "
         "HTTP Basic credentials for the duration of the session (and resend "
         "them automatically!), so "
         (element/link-to (util/context-uri req "/logout") "logging out")
          " won't work as expected."]
     [:p "You can access resources requiring HTTP Basic authentication "
         "trivially in any HTTP client (like `curl`) with a URL such as:"]
     [:p [:code "curl "
         (str/replace (str (util/request-url req) "/requires-authentication")
           #"://" #(str % (-> @users first val :username (str ":clojure@"))))]]
     footer)))

(defn interactive-form-page
  [req]
  (hiccup.page/html5
    head
    (body
     (github-link req)
     [:h2 (-> req :demo :name)]
     [:p {:class "lead"}
         "This app demonstrates typical username/password authentication, and "
         "a pinch of Friend's authorization capabilities."]
     (get-user-status req)
     login-form
     (get-protected-links req)
     (logging-out-section req))))

(defn multifactor-page
  [req]
  (hiccup.page/html5
    head
    (body
      (github-link req)
      [:h2 (-> req :demo :name)]
      [:p {:class "lead"}
         "This app demonstrates a means of using multi-factor authentication, "
         "and a pinch of Friend's authorization capabilities."]
      (get-user-status req)
      [:div {:class "alert alert-info"}
       [:p {:class "lead"}
         "Note: the PIN for this demo is always `1234`."]]
      [:p {:class "lead"}
        "Clicking "
        (element/link-to
          (util/context-uri req "requires-authentication") "this link") " "
        "will start a multi-factor authentication process, simluating one "
        "where a random PIN is sent to you via SMS."]
      (get-protected-links req)
      (logging-out-section req))))
