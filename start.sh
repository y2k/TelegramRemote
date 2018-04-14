$ANDROID_HOME/platform-tools/adb reverse tcp:8081 tcp:8081
$ANDROID_HOME/platform-tools/adb reverse tcp:3449 tcp:3449

re-natal use-android-device real
# re-natal use-android-device avd
re-natal use-figwheel

react-native run-android
# lein figwheel android
lein repl