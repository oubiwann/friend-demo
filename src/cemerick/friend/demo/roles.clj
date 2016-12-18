(ns cemerick.friend.demo.roles)

(def user ::user)
(def admin ::admin)

(derive admin user)
