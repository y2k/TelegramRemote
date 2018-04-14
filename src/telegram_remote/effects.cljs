(ns telegram-remote.effects
  (:require
   [re-frame.core :refer [reg-event-db reg-event-fx reg-fx dispatch]]
   [telegram-remote.db :as db :refer [app-db]]
   [telegram-remote.domain :as domain]))

(def ReactNative (js/require "react-native"))
(def async-storage (.-AsyncStorage ReactNative))
(def android (.-Android (.-NativeModules ReactNative)))

; (.addEventListener (.-AppState ReactNative) "change" #(dispatch [:initialize-app]))
; (defn alert [title] (.alert (.-Alert ReactNative) "TelegramRemote" title))

;; Events

(reg-event-fx :initialize-app (fn [] {:initialize-app-fx nil}))

(reg-event-fx :open-settings (fn [_] {:open-intent "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"}))
(reg-event-fx :open-telegram (fn [_] {:open-intent "https://t.me/BotFather"}))

(reg-event-fx :validate-connection (fn [_ [_ db]] {:validate-connection-fx db}))

(reg-event-fx
 :send-to-telegram
 (fn [cfx [_ data]]
   {:send-to-telegram-fx data}))

(reg-event-fx
 :handle-telegram-msg
 (fn [{db :db} [_ {user-id :user-id in-message :message}]]
   (let [{message :message newIds :ids}
         (domain/handleTelegramMsg (:connected-ids db) user-id in-message (:pincode db))]
     {:send-to-telegram-fx {:user-id user-id :message message}
      :db (assoc db :connected-ids newIds)
      :sync-user-ids newIds})))

;; Effects

; (reg-fx :platform
;         (fn [{method :method args :args}]
;           (js-invoke android method args)))

(reg-fx :open-intent
        (fn [url]
          (if (= url "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
            (.openSettings android)
            (.openURL (.-Linking (js/require "react-native")) url))))

(defn load-external-state []
  (.getEnvironment
   android
   #(dispatch [:update-db {:secure-id (.-secure-id %)
                           :notification-listeners (.-notification-listeners %)}])))

; (reg-fx :initialize-app-fx
;         (fn []
;           ; (.setItem as "k" (.stringify js/JSON (clj->js x)))
;           ; (.then (.getItem as "k") (fn [y] (println (js->clj y))))
;           ; (.then (.getItem as "k") (fn [y] (println (.parse js/JSON y))))
;           ; (.then (.getItem as "k") (fn [x] (->> x (.parse js/JSON) (js->clj) (println))))
;           ; (->> {:a "bb"} (clj->js) (.stringify js/JSON) (.parse js/JSON) (js->clj))

;           (.then
;            (.getItem async-storage "token")
;            (fn [token]
;              (dispatch [:db (assoc db :token token)])))
;           ; (.getState
;           ;  android
;           ;  #(dispatch [:update-db {:token (.-token %)}]))
;           (.getAndroidId
;            android
;            #(dispatch [:update-db {:pincode (domain/toPincode %)}]))
;           (.getNotificationListeners
;            android
;            #(dispatch [:update-db {:is-listen-notifications (domain/checkIsListenNotifications %)}]))))

(reg-fx :validate-connection-fx
        (fn [db]
          (.restartListener android (:temp-token db))
          (dispatch [:update-db {:token (:temp-token db) :temp-token ""}])))

(reg-fx
 :send-to-telegram-fx
 (fn [{user-id :user-id message :message}]
   (.sendToTelegram android user-id message)))

(reg-fx :sync-user-ids #(.saveUserIds android (clj->js %)))