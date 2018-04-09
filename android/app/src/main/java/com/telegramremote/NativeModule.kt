package com.telegramremote

import android.annotation.SuppressLint
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Toast
import com.facebook.react.bridge.*
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.request.SendMessage
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

class NativeModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val prefs = reactApplicationContext.getSharedPreferences("users", 0)

    @ReactMethod
    fun loadUserIds(callback: Callback) {
        val ids = prefs.getStringSet("ids", emptySet())
            .toList().let(Arguments::fromList)
        callback.invoke(ids)
    }

    @ReactMethod
    fun saveUserIds(ids: ReadableArray) {
        prefs.edit()
            .putStringSet("ids", ids.toArrayList().filterIsInstance<String>().toSet())
            .apply()
    }

    @ReactMethod
    fun sendToTelegram(userId: String, message: String) {
        val token = prefs.getString("token", null)!!
        TelegramBot(token).execute(SendMessage(userId, message))
    }

    @ReactMethod
    fun restartListener(token: String) {
        prefs.edit().putString("token", token).apply()
        TelegramLister.reset(prefs, reactApplicationContext)
    }

    @ReactMethod
    fun getState(callback: Callback) {
        val token = prefs.getString("token", "")

        callback(
            Arguments.createMap().apply {
                putString("token", token)
            })
    }

    @SuppressLint("HardwareIds")
    @ReactMethod
    fun getAndroidId(callback: Callback) {
        val secureId = Settings.Secure.getString(
            reactApplicationContext.contentResolver, Settings.Secure.ANDROID_ID)
        callback(secureId)
    }

    @ReactMethod
    fun getNotificationListeners(callback: Callback) {
        val listeners = Settings.Secure.getString(
            reactApplicationContext.contentResolver, "enabled_notification_listeners")
        callback(listeners)
    }

    @ReactMethod
    fun openSettings() {
        currentActivity!!.startActivity(
            Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
    }

    @ReactMethod
    fun toast(message: String) =
        Toast.makeText(reactApplicationContext, message, Toast.LENGTH_LONG).show()

    override fun getName(): String = "Android"
}

class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.i("TelegramLister", "onNotificationPosted(sbn = $sbn)")
        launch(UI) {
            try {
                sendToAll(sbn.notification.tickerText!!.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun sendToAll(text: String) {
        val prefs = getSharedPreferences("users", 0)
        val token = prefs.getString("token", null)!!

        Log.i("TelegramLister", "sendToAll(token = $token, ids = ${prefs.getStringSet("ids", emptySet())})")

        prefs.getStringSet("ids", emptySet())
            .forEach {
                withContext(DefaultDispatcher) {
                    TelegramBot(token).execute(SendMessage(it, text))
                }
            }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) = Unit
}