(ns telegram-remote.domain)

(defn checkIsListenNotifications [listeners]
  (clojure.string/includes? listeners "com.telegramremote/com.telegramremote.NotificationListener"))

(defn toPincode [android-id]
  (-> android-id (hash) (mod 10000) (str)))

(defn handleTelegramMsg [connected-ids user-id message pincode]
  (if (contains? connected-ids user-id)
    {:message "Вы уже подключили телефон" :ids connected-ids}
    (if (= message pincode)
      {:message "pincode верен" :ids (conj connected-ids user-id)}
      {:message "pincode неправильный" :ids connected-ids})))