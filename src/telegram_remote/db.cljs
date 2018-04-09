(ns telegram-remote.db
  (:require
   [re-frame.core :refer [reg-event-db reg-sub]]))

;; initial state of app-db
(def app-db {:pincode "0000"
             :connected-ids #{}
             :token ""
             :temp-token ""
             :is-listen-notifications false
             :isBusy false})

(reg-event-db :initialize-db (fn [_ _] app-db))

(reg-sub :get-db (fn [db _] db))

(reg-event-db :update-db
              (fn [db [_ value]] (merge db value)))