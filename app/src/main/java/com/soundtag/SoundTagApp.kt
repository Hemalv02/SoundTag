package com.soundtag

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.soundtag.service.UploadWorker

class SoundTagApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        UploadWorker.enqueueIfPending(this)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_RECORDING,
            "Recording",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows recording status"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_RECORDING = "recording_channel"
    }
}
