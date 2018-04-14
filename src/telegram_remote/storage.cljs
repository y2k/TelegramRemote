(ns telegram-remote.storage)

(def async-storage (.-AsyncStorage (js/require "react-native")))

(defn to-keyword-map [x]
  (into {} (for [[k v] x] [(keyword k) v])))

(defn load [callback]
  (.then (.getItem async-storage "default")
         (fn [json]
           (callback (to-keyword-map (or (js->clj (.parse js/JSON json)) {}))))))

(defn save [value]
  (.setItem async-storage "default" (.stringify js/JSON (clj->js value))))