(ns telegram-remote.android.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [telegram-remote.effects :as effects]))

(def ReactNative (js/require "react-native"))
(def app-registry (.-AppRegistry ReactNative))
(def text (r/adapt-react-class (.-Text ReactNative)))
(def text-input (r/adapt-react-class (.-TextInput ReactNative)))
(def view (r/adapt-react-class (.-View ReactNative)))
(def image (r/adapt-react-class (.-Image ReactNative)))
(def activity-indicator (r/adapt-react-class (.-ActivityIndicator ReactNative)))
(def touchable-native (r/adapt-react-class (.-TouchableNativeFeedback ReactNative)))
(def scroll-view (r/adapt-react-class (.-ScrollView ReactNative)))

(defn button [title on-click]
  [touchable-native
   {:background (.Ripple (.-TouchableNativeFeedback ReactNative) "white")
    ; :useForeground true
    :on-press on-click}
   [view {:pointerEvents "box-only"}
    [text {:style {:color "white" :text-align "center" :font-weight "bold"
                   :border-radius 5 :padding 10
                   :background-color "#2bbafd"}} title]]])

(defn app-root []
  (let [db (subscribe [:main-page])]
    (fn []
      [view {:style {:background-color "#192431" :flex 1}}
       [text {:style {:background-color "#243445" :textAlignVertical "center"
                      :padding-left 10 :height 50 :color "white" :fontSize 20}}
        "Настроить соединение"]

       [scroll-view {:style {:flex-direction "column" :padding-horizontal 20 :padding-vertical 5}}

        [text {:style {:color "white" :margin-vertical 8}}
         "Пинкод для доступа:"]
        [text {:style {:color "white" :margin-vertical 8 :font-size 24 :align-self "center"}}
         (:pincode @db)]

        [text {:style {:color "white" :margin-vertical 8}} "Дать доступ к нотификациям"]
        [button
         (str "Открыть настройки " (if (:is-listen-notifications @db) "(Enabled)"))
         #(dispatch [:open-settings])]

        [text {:style {:color "white" :margin-vertical 8}} "Создать бота"]
        [button "Открыть telegram" #(dispatch [:open-telegram])]

        [text {:style {:color "white" :margin-vertical 8}} "Введите Access-Token"]
        [text-input {:placeholder (or (not-empty (:token @db)) "Access-Token бота")
                     :placeholderTextColor "#aaa" :underlineColorAndroid "white"
                     :style {:color "white"}
                     :on-change-text (fn [x] (dispatch [:update-db {:temp-token x}]))}
         (:temp-token @db)]

        [button "Сохранить" (fn [] dispatch [:validate-connection @db])]]])))

(defn init []
  (dispatch-sync [:initialize-db])
  (effects/load-external-state)
  (.registerComponent app-registry "TelegramRemote" #(r/reactify-component app-root)))

(.registerHeadlessTask app-registry "ClojureApp"
                       (fn []
                         (fn [data]
                           (dispatch [:handle-telegram-msg {:user-id (.-user data) :message (.-message data)}])
                           (.resolve js/Promise ""))))