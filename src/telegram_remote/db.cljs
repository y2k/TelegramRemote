(ns telegram-remote.db
  (:require
   [re-frame.core :refer [reg-event-db reg-sub]]
   [telegram-remote.domain :as domain]))

;; initial state of app-db
(def app-db {:secure-id ""
             :notification-listeners ""

             :connected-ids #{}
             :token ""
             :temp-token ""

             :is-listen-notifications false
             :pincode "0000"})

(reg-event-db :initialize-db (fn [_ _] app-db))

(reg-sub :get-db (fn [db _] db))

(reg-event-db :update-db
              (fn [db [_ value]] (merge db value)))

(reg-sub :main-page
         (fn [db _]
           {:pincode (domain/toPincode (:secure-id db))
            :token (:token db)
            :temp-token (:temp-token db)
            :is-listen-notifications (domain/checkIsListenNotifications (:notification-listeners db))
            }))