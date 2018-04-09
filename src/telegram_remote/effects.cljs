(ns telegram-remote.effects
  (:require
   [re-frame.core :refer [reg-event-db reg-event-fx reg-fx dispatch]]
   [telegram-remote.db :as db :refer [app-db]]
   [telegram-remote.domain :as domain]))

(def ReactNative (js/require "react-native"))
(def async-storage (.-AsyncStorage ReactNative))
(def android (.-Android (.-NativeModules ReactNative)))
(defn alert [title]
  (.alert (.-Alert ReactNative) "TelegramRemote" title))

(.addEventListener (.-AppState ReactNative) "change" #(dispatch [:initialize-app]))

; Effects

(reg-event-fx :initialize-app
              (fn [] {:initialize-app-fx nil}))

(reg-fx :initialize-app-fx
        (fn []
          ; (.setItem as "k" (.stringify js/JSON (clj->js x)))
          ; (.then (.getItem as "k") (fn [y] (println (js->clj y))))

          ; (.then (.getItem as "k") (fn [y] (println (.parse js/JSON y))))

          ; (.then (.getItem as "k") (fn [x] (->> x (.parse js/JSON) (js->clj) (println))))

          ; (->> {:a "bb"} (clj->js) (.stringify js/JSON) (.parse js/JSON) (js->clj))

          (.then
           (.getItem async-storage "token")
           (fn [token]
             (dispatch [:db (assoc db :token token)])))
          ; (.getState
          ;  android
          ;  #(dispatch [:update-db {:token (.-token %)}]))
          (.getAndroidId
           android
           #(dispatch [:update-db {:pincode (domain/toPincode %)}]))
          (.getNotificationListeners
           android
           #(dispatch [:update-db {:is-listen-notifications (domain/checkIsListenNotifications %)}]))))

(reg-event-fx :open-settings (fn [_] {:open-settings-fx nil}))

(reg-fx :open-settings-fx #(.openSettings android))

(reg-event-fx :open-telegram (fn [_] {:open-url-fx "https://t.me/BotFather"}))

(reg-fx :open-url-fx #(.openURL (.-Linking (js/require "react-native")) %))

(reg-event-fx :validate-connection (fn [_ [_ db]] {:validate-connection-fx db}))

(reg-fx :validate-connection-fx
        (fn [db]
          (.restartListener android (:temp-token db))
          (dispatch [:update-db {:token (:temp-token db) :temp-token ""}])))

(reg-event-db :test-set-isbusy (fn [db _] (assoc db :isBusy true)))

(reg-event-fx
 :send-to-telegram
 (fn [cfx [event data]]
   (.log js/console (str "Coeffect = " cfx))
   (.log js/console (str "Event = " event))
   (.log js/console (str "Data = " data))
   {:send-to-telegram-fx data}))

(reg-fx
 :send-to-telegram-fx
 (fn [{user-id :user-id message :message}]
   (.sendToTelegram android user-id message)))

(reg-fx :sync-user-ids #(.saveUserIds android (clj->js %)))

(reg-event-fx
 :handle-telegram-msg
 (fn [{db :db} [_ {user-id :user-id in-message :message}]]
   (let [{message :message newIds :ids}
         (domain/handleTelegramMsg (:connected-ids db) user-id in-message (:pincode db))]
     {:send-to-telegram-fx {:user-id user-id :message message}
      :db (assoc db :connected-ids newIds)
      :sync-user-ids newIds})))