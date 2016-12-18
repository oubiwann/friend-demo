(ns cemerick.friend.demo.users
  (:require [cemerick.friend.demo.roles :as roles]
            [cemerick.friend.credentials :refer (hash-bcrypt)]))

(def users (atom {"friend" {:username "friend"
                            :password (hash-bcrypt "clojure")
                            :pin "1234" ;; only used by multi-factor
                            :roles #{roles/user}}
                  "friend-admin" {:username "friend-admin"
                                  :password (hash-bcrypt "clojure")
                                  :pin "1234" ;; only used by multi-factor
                                  :roles #{roles/admin}}}))
