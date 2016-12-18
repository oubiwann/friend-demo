(ns cemerick.friend.demo.content
  (:require [clojure.string :as str]
            [cemerick.friend.demo.users :refer [users]]
            [cemerick.friend.demo.util :as util]
            [hiccup.core]
            [hiccup.element :as element]
            [hiccup.page]))

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
   [:p "…a collection of demonstration apps using "
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
       [:p {:class "lead"}
           "So, what better than a bunch of live
           demos of each authentication workflow that Friend supports (or is
           available via another library that builds on top of Friend), with
           smatterings of authorization examples here and there, all with links
           to the generally-less-than-10-lines of code that makes it happen?"]
       [:p {:class "lead"}
           "Check out the demos, find the one(s) that apply to your situation,
           and click the button on the right to go straight to the source for
           that demo:"]
       [:div {:class "columns small-8"}
        [:h2 "Demonstrations"]
        [:ol
         (for [{:keys [name doc route-prefix]} apps]
           [:li (element/link-to (str route-prefix "/") [:strong name])
            " — " doc])]]
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
     [:p "You can access resources requiring HTTP Basic authentication trivially in "
         "any HTTP client (like `curl`) with a URL such as:"]
     [:p [:code "curl "
         (str/replace (str (util/request-url req) "/requires-authentication")
           #"://" #(str % (-> @users first val :username (str ":clojure@"))))]]
     footer)))

(defn http-basic-footer
  [req]
  [:p "You can combine this with Friend's \"channel security\" middleware to enforce the "
      "use of SSL, making this a good recipe for controlling access to web service APIs."
      " Head over to "
      (element/link-to (util/context-uri req "/https-basic") "HTTPS Basic")
      " for a demo."])

(defn https-basic-footer
  [req]
  [:p "Note that because the handler that requires authentication is "
      "further guarded by "
      [:code "cemerick.friend/requires-scheme"]
      ", all requests to it are redirected over HTTPS "
      "(even before the HTTP Basic challenge is sent, if required)."])
