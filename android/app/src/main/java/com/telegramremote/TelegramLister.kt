package com.telegramremote

import android.content.Context
import android.content.SharedPreferences
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.UpdatesListener.CONFIRMED_UPDATES_ALL
import com.pengrad.telegrambot.model.Update
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch

object TelegramLister {

    private var job: Job? = null

    fun start(context: Context) {
        restart(context.getSharedPreferences("users", 0), context)
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    fun reset(prefs: SharedPreferences, token: String, pincode: String, context: Context) {
        prefs.edit().putString("token", token).putString("pincode", pincode).apply()
        if (job != null) restart(prefs, context)
    }

    private fun restart(prefs: SharedPreferences, context: Context) {
        job?.cancel()

        val token = prefs.getString("token", "")
        job = launch {
            val bot = TelegramBot(token)
            bot.setUpdatesListener {
                try {
                    for (x in it) handleUpdate(context, prefs, x)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                CONFIRMED_UPDATES_ALL
            }

            try {
                delay(Int.MAX_VALUE)
            } finally {
                bot.removeGetUpdatesListener()
            }
        }
    }

    private fun handleUpdate(context: Context, prefs: SharedPreferences, update: Update) {
        MyTaskService.handleTelegramMessage(
            context,
            update.message().from().id().toString(),
            update.message().text(),
            prefs.getStringSet("ids", emptySet()))

//        if (prefs.contains(id)) return
//
//        if (isValidPinCode(update.message().text(), pincode)) {
//            val ids = prefs.getStringSet("ids", emptySet())
//            prefs.edit().putStringSet("ids", ids + id).apply()
//            bot.execute(SendMessage(id, "Вы успешно подключили телефон"))
//        } else {
//            bot.execute(SendMessage(id, "Что бы подключить телефон, введит пинкод (4 цифры)"))
//        }
    }
}