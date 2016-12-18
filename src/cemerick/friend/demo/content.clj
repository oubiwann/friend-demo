(ns cemerick.friend.demo.content
  (:require [cemerick.friend.demo.util :as util]
            [clojure.string :as str]
            [hiccup.core :as h]
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
  (h/html
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
