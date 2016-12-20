(ns cemerick.friend.demo.content
  (:require [clojure.string :as string]
            [cemerick.friend :as friend]
            [cemerick.friend.demo.content.fragment :as fragment]
            [cemerick.friend.demo.users :refer [users]]
            [cemerick.friend.demo.util :as util]
            [hiccup.element :as element]
            [hiccup.page :as page]))

;;; Sub-pages for apps

(defn login-page
  [req]
  (page/html5
    fragment/head
    (fragment/body fragment/login-form)))

(defn user-page
  [req]
  (page/html5
    fragment/head
    (fragment/body
     [:h2 (-> req :demo :name)]
     [:p {:class "lead"} "You're a user!"])))

(defn admin-page
  [req]
  (page/html5
    fragment/head
    (fragment/body
     [:h2 (-> req :demo :name)]
     [:p {:class "lead"} "You're an admin!"])))

(defn authed-page
  [req]
  (page/html5
    fragment/head
    (fragment/body
     [:h2 (-> req :demo :name)]
     [:p {:class "lead"} "Thanks for authenticating!"])))

(defn unauthed-page
  [req uri]
  (page/html5
    fragment/head
    (fragment/body
     [:h2 (-> req :demo :name)]
     [:div {:class "alert alert-danger"}
     [:p {:class "lead"}
       "You do not have sufficient privileges to access "
       uri]])))

(defn multifactor-login-page
  [req]
  (page/html5
    fragment/head
    (fragment/body
    [:h2 (-> req :demo :name)]
    [:form {:class "form-horizontal" :method "POST" :action "start"}
     [:fieldset
      [:legend "Login"]
      fragment/email-input
      (fragment/login-button)]])))

(defn pin-page
  [req identity invalid-login?]
  (page/html5
    fragment/head
    (fragment/body
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
      fragment/multifactor-finish-form)))

;;; Top-level and App Pages

(defn landing-page
  [apps]
  (page/html5
    fragment/head
    (fragment/body
     [:h1
      [:a {:href "http://github.com/cemerick/friend-demo"} "Among Friends"]]
     (fragment/jumbotron)
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
                " — associated with an \"admin\" role"]]])))

(defn http-basic-page
  [req footer]
  (page/html5
    fragment/head
    (fragment/body
     (fragment/github-link req)
     [:h2 (-> req :demo :name)]
     [:p {:class "lead"}
         "Attempting to access "
         (element/link-to {:id "interactive_url"}
           (util/context-uri req "requires-authentication") "this link ")
         "will issue a challenge for your user-agent (browser) to provide "
         "HTTP Basic credentials. Once authenticated, all the authorization "
         "options available in Friend are available to restrict the "
         "permissions of particular users."]
     (fragment/get-user-status req)
     [:div {:class "alert alert-info"}
      [:p {:class "lead"}
        "Note: that Chrome (and maybe other browsers) silently save "
        "HTTP Basic credentials for the duration of the session (and resend "
        "them automatically!), so "
        (element/link-to (util/context-uri req "/logout") "logging out")
        " won't work as expected."]]
     (fragment/get-protected-links req)
     [:p "You can access resources requiring HTTP Basic authentication "
         "trivially in any HTTP client (like `curl`) with a URL such as:"]
     [:p [:code "curl "
         (string/replace (str (util/request-url req) "/requires-authentication")
           #"://" #(str % (-> @users first val :username (str ":clojure@"))))]]
     footer
     (fragment/logging-out-section req))))

(defn interactive-form-page
  [req]
  (page/html5
    fragment/head
    (fragment/body
     (fragment/github-link req)
     [:h2 (-> req :demo :name)]
     [:p {:class "lead"}
         "This app demonstrates typical username/password authentication, and "
         "a pinch of Friend's authorization capabilities."]
     (fragment/get-user-status req)
     fragment/login-form
     (fragment/get-protected-links req)
     (fragment/logging-out-section req))))

(defn multifactor-page
  [req]
  (page/html5
    fragment/head
    (fragment/body
      (fragment/github-link req)
      [:h2 (-> req :demo :name)]
      [:p {:class "lead"}
         "This app demonstrates a means of using multi-factor authentication, "
         "and a pinch of Friend's authorization capabilities."]
      (fragment/get-user-status req)
      [:div {:class "alert alert-info"}
       [:p {:class "lead"}
         "Note: the PIN for this demo is always `1234`."]]
      [:p {:class "lead"}
        "Clicking "
        (element/link-to
          (util/context-uri req "requires-authentication") "this link") " "
        "will start a multi-factor authentication process, simluating one "
        "where a random PIN is sent to you via SMS."]
      (fragment/get-protected-links req)
      (fragment/logging-out-section req))))

(defn github-oauth2-page
  [req get-user-handle get-user-details]
  (page/html5
    fragment/head
    (fragment/body
      (fragment/github-link req)
      [:h2 (-> req :demo :name)]
      [:div {:class "alert alert-danger"}
       [:p {:class "lead"}
         "Warning: this demo is very experimental for now."]]
      [:p {:class "lead"}
         "This app demonstrates typical username/password authentication, and "
         "a pinch of Friend's authorization capabilities."]
      (fragment/get-oauth2-user-status req get-user-handle)
      (when-let [{access-token :access_token} (friend/current-authentication req)]
        [:div
         [:p {:class "lead"}
           "Some of your public repositories on GitHub,
           obtained using the access token above:"]
         [:ul (for [repo (get-user-details access-token)]
                [:li (:full-name repo)])]])
      (fragment/get-protected-links req)
      (fragment/logging-out-section req))))

(defn openid-page
  [req providers]
  (page/html5
    fragment/head
    (fragment/body
      (fragment/github-link req)
      [:h2 (-> req :demo :name)]
      [:p {:class "lead"}
         "Authenticating with various services using OpenID."]
      (fragment/get-openid-user-status req)
      (if-let [auth (friend/current-authentication req)]
        [:div
         [:p {:class "lead"}
           "Some information delivered by your OpenID provider:"]
         [:ul (for [[k v] auth
                    :let [[k v] (if (= :identity k)
                                  ["Your OpenID identity"
                                   (str (subs v 0 (* (count v) 2/3)) "...")]
                                  [k v])]]
                [:li [:strong (str (name k) ": ")] v])]]
         [:div
           (fragment/openid-supported-service-login req providers)
           [:p {:class "lead"}
             "... or, with a user-provided OpenID URL:"]
           (fragment/openid-freeform-service-login req)])
      (fragment/get-protected-links req)
      (fragment/logging-out-section req))))

(defn signup-form-page
  [req]
  (page/html5
    fragment/head
    (fragment/body
     (fragment/github-link req)
     [:h2 (-> req :demo :name)]
     (when-let [msg (:flash req)]
       [:div {:class "alert alert-danger"}
         [:p {:class "lead"} msg]])
     [:p {:class "lead"}
         "Any user/pass combination will do, as you are creating a new
         account or profile."]
     (fragment/get-user-status req)
     fragment/signup-form
     (fragment/get-protected-links req)
     (fragment/logging-out-section req))))

(defn signed-up-user-page
  [req user]
  (page/html5
    fragment/head
    (fragment/body
     (fragment/github-link req)
     [:h2 (-> req :demo :name)]
     [:p {:class "lead"}
       (format "Hello, new user %s!" user)]
     (fragment/get-user-status req)
     [:p {:class "lead"}
       "Return to the " (element/link-to (util/context-uri req "") "example, "
       "or "  (element/link-to (util/context-uri req "logout") "log out") ".")]
     (fragment/get-protected-links req)
     (fragment/logging-out-section req))))
