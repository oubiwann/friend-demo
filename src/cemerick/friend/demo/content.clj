(ns cemerick.friend.demo.content
  (:require [clojure.string :as str]
            [cemerick.friend.demo.misc :as misc]))

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

(defn github-link
  [req]
  [:div {:style "float:right; width:50%; margin-top:1em; text-align:right"}
   [:a {:class "button" :href (misc/github-url-for (-> req :demo :ns-name))}
       "View source"]
   " | "
   [:a {:class "button secondary" :href "/"} "All demos"]])
