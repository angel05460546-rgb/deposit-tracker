package com.example.deposittracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class SmsReceiver : BroadcastReceiver() {

    // اگه بخواید فقط پیامک‌های یه شماره‌ی خاص بررسی بشن، بخشی از اون شماره رو اینجا بذارید.
    private val senderKeyword = ""
    private val bodyKeywords = listOf("واریز", "blu", "بلو")

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        for (msg in messages) {
            val sender = msg.originatingAddress ?: ""
            val body = msg.messageBody ?: ""

            if (senderKeyword.isNotEmpty() && !sender.contains(senderKeyword, true)) continue
            if (bodyKeywords.none { body.contains(it, true) }) continue

            val amount = extractAmount(body) ?: continue
            Store.addEntry(context, amount, "پیامک")
        }
    }

    private fun extractAmount(text: String): Long? {
        val regex = Regex("[\\d,]{4,}")
        return regex.findAll(text)
            .map { it.value.replace(",", "") }
            .mapNotNull { it.toLongOrNull() }
            .filter { it >= 1000 }
            .maxOrNull()
    }
}
