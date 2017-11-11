package nz.zhang.lecturerecordingplayer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.tonyodev.fetch.Fetch
import nz.zhang.lecturerecordingplayer.recordings.RecordingStore


class LectureRecordingApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // This fires on first launch

        // Remove all Fetch requests in db
        Fetch.newInstance(applicationContext).removeRequests()

        // Load recordings from sharedprefs
        RecordingStore.sharedPrefs = getSharedPreferences(PREFS_NAME, 0)
        RecordingStore.loadList()

        // Setup notification channel if we're on O R E O
        if (Build.VERSION.SDK_INT >= 26) {
            val nManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel("downloads", "Download Status", NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = "Status notifications for recording downloads"
            channel.enableVibration(true)
            nManager.createNotificationChannel(channel)
        }

    }
}