package com.telegramremote

import android.content.Context
import android.content.Intent
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.bridge.Arguments
import com.facebook.react.jstasks.HeadlessJsTaskConfig

class MyTaskService : HeadlessJsTaskService() {

    override fun getTaskConfig(intent: Intent): HeadlessJsTaskConfig? {
        val extras = intent.extras ?: return null
        return HeadlessJsTaskConfig(
            "ClojureApp", Arguments.fromBundle(extras),
            2000, true)
    }

    companion object {

        fun handleTelegramMessage(context: Context, userId: String, message: String, ids: Set<String>) {
            Intent(context, MyTaskService::class.java)
                .putExtra("user", userId)
                .putExtra("message", message)
                .putExtra("ids", ids.toTypedArray())
                .let(context::startService)
        }
    }
}