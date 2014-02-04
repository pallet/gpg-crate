(ns pallet.crate.gpg
  "Install gpg"
  (:require
   [pallet.core.session :as session]
   [pallet.script.lib :as lib]
   [pallet.stevedore :as stevedore])
  (:use [pallet.actions :only [package packages package-manager directory
                               remote-file exec-script exec-checked-script]]
        [pallet.crate :only [defplan]]
        pallet.thread-expr))

(defplan gpg
  "Install from packages"
  []
  (packages :aptitude ["pgpgpg"]))

(defplan import-key
  "Import key. Content options are as for remote-file."
  [& {:keys [user] :as options}]
  (let [path "gpg-key-import"
        user (or user (-> (session/session) :user :username))
        home (stevedore/script (~lib/user-home ~user))
        dir (str home "/.gnupg")]
    (directory dir :mode "0700" :owner user)
    (apply
     remote-file
     path (apply concat (merge {:mode "0600" :owner user} options)))
    (exec-checked-script
     "Import gpg key"
     (sudo -u ~user gpg -v -v "--homedir" ~dir "--import" ~path))
    (remote-file path :action :delete :force true)))

(defplan list-keys
  "List keys for user"
  [& {:keys [user] :as options}]
  (let [user (or user (-> (session/session) session/admin-user :username))]
    (exec-checked-script
     "list gpg keys for user"
     (sudo -u ~user gpg "--list-keys"))))
