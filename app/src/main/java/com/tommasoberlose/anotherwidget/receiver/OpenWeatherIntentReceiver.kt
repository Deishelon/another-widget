package com.tommasoberlose.anotherwidget.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tommasoberlose.anotherwidget.`object`.Constants
import com.tommasoberlose.anotherwidget.util.Util

class OpenWeatherIntentReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Constants.ACTION_OPEN_WEATHER_INTENT) {
            context.sendBroadcast(Intent(Constants.ACTION_WEATHER_UPDATE))
            context.startActivity(Util.getWeatherIntent(context))
        }
    }
}
