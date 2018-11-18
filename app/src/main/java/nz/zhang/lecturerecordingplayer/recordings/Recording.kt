package nz.zhang.lecturerecordingplayer.recordings

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.arch.persistence.room.Entity
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.support.annotation.NonNull
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.tonyodev.fetch.Fetch
import com.tonyodev.fetch.listener.FetchListener
import com.tonyodev.fetch.request.Request
import nz.zhang.lecturerecordingplayer.RECORDING_FROMSORTED
import nz.zhang.lecturerecordingplayer.RECORDING_ID
import nz.zhang.lecturerecordingplayer.RecordingViewActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


const val COURSENAME_REGEX: String = "(?<=(/))[A-Z]{4,8}(?=(\\d{3}\\w{4,5}/))"
const val COURSENUMBER_REGEX: String = "(?<=(/[A-Z]{4,8}))\\d{3}(?=(\\w{4,5}/))"
const val COURSESTREAM_REGEX: String = "(?<=(/[A-Z]{4,8}\\d{3}))\\w{4,5}(?=(/))"
const val SEMESTERNUMBER_REGEX: String = "(?<=(\\/))\\d{4}(?=(\\/[A-Z]{4,8}\\d{3}\\w{4,5}\\/))"

const val SUFFIX_REGEX: String = "(?<=(\\/\\d{12}\\.LT\\d{6}\\.))\\w*"

const val FILEEXTENSION_REGEX: String = "(\\.preview|\\.mp4|\\.m4v|\\.mp3|-slides\\.m4v)\$"

const val TIME_REGEX: String = "(?<=(\\/))\\d{12}(?=(\\.))"

@Entity(primaryKeys = arrayOf("courseName", "courseNumber", "courseStream", "semesterNumber", "recordingDate"), tableName = "recordings")
class Recording(url: String) : Comparable<Recording> {
    @NonNull
    val courseName: String
    @NonNull
    val courseNumber: String
    @NonNull
    val courseStream: String
    @NonNull
    val semesterNumber: String
    @NonNull
    val recordingDate: Date
    @NonNull
    val recordingSuffix: String

    val urlNoExtension: String

    @Transient private val statusListeners: ArrayList<RecordingStatusListener> = ArrayList()

    @Transient var downloading = false
    @Transient var downloaded = false
    @Transient var downloadHQ = false

    @Transient var isValid = true // whether the recording is a valid url

    @Transient var dlProgress = 0
    @Transient var dlError = false

    @Transient var downloadID: Long = -1

    init {
        // Example: 201707281200
        val dateFormat = SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault())
        // Read the course name
        val name = COURSENAME_REGEX.toRegex().find(url)?.value
        val number = COURSENUMBER_REGEX.toRegex().find(url)?.value
        val stream = COURSESTREAM_REGEX.toRegex().find(url)?.value
        val semester = SEMESTERNUMBER_REGEX.toRegex().find(url)?.value

        val extensionless = FILEEXTENSION_REGEX.toRegex().replace(url, "")

        val time = TIME_REGEX.toRegex().find(url)?.value

        if (name != null && number != null && stream != null && time != null && semester != null) {
            Log.d("foundName", name)
            courseName = name
            courseNumber = number
            courseStream = stream
            urlNoExtension = extensionless
            semesterNumber = semester
            recordingDate = dateFormat.parse(time)
            isValid = true
            if (recordingDate.after(Date())) // If it's later than right now, then that's broken
                isValid = false
        } else {
            Log.e("BadURL", url)
            isValid = false
            courseName = "Not found"; courseStream = "Not found"; urlNoExtension = "Not found"; courseNumber = "Not found"; recordingDate = Date(); semesterNumber = "0000"
        }

        recordingSuffix = SUFFIX_REGEX.toRegex().find(url)?.value ?: ""
    }

    // Used for GSON reasons
    private constructor() : this("")

    @SuppressLint("SimpleDateFormat")
    override fun toString(): String {
        val df = SimpleDateFormat("yyyy-MM-dd_HHmm")
        return "${courseName}_${courseNumber}_${df.format(recordingDate)}$recordingSuffix"
    }

    fun addListener(listener: RecordingStatusListener) {
        statusListeners.add(listener)
        sendUpdate()
    }

    private fun sendUpdate() {
        for (listener: RecordingStatusListener in statusListeners) listener.update(downloading, downloaded, dlProgress, dlError)
    }

    override fun equals(other: Any?): Boolean {
        if (other is Recording) {
            return urlNoExtension == other.urlNoExtension
        } else {
            return false
        }
    }

    override fun hashCode(): Int {
        return Objects.hash(urlNoExtension)
    }

    override fun compareTo(other: Recording): Int {
        return recordingDate.compareTo(other.recordingDate)
    }

    fun getFile() : File {
        return File("${Environment.getExternalStorageDirectory()}/Download/Lecture Recordings/${toString()}.mp4")
    }

    fun getFileHQ() : File {
        return File("${Environment.getExternalStorageDirectory()}/Download/Lecture Recordings/${toString()}_HQ.mp4")
    }

    fun delete() {
        if (getFile().delete()) downloaded = false
        if (getFileHQ().delete()) downloaded = false
        sendUpdate()
    }

    fun checkFS() {
        val file = getFile()
        val hqfile = getFileHQ()
        downloaded = if (file.isFile && file.length() > 10000) {
            Log.d("FileCheck", "${Environment.getExternalStorageDirectory()}/Download/Lecture Recordings/${toString()}.mp4 Exists")
            downloadHQ = false
            true
        } else if (file.isFile && file.length() < 10000) {
            // Delete empty files
            file.delete()
            false
        } else if (hqfile.isFile && hqfile.length() > 10000) {
            Log.d("FileCheck", "${Environment.getExternalStorageDirectory()}/Download/Lecture Recordings/${toString()}_HQ.mp4 Exists")
            downloadHQ = true
            true
        } else if (hqfile.isFile && hqfile.length() < 10000) {
            // Delete empty files
            hqfile.delete()
            false
        } else {
            false
        }
        //Log.d("FileCheck", "${Environment.getExternalStorageDirectory()}/Download/Lecture Recordings/${toString()}.mp4 Doesn't Exist")
    }

    fun niceName():String {
        return "$courseName $courseNumber ($courseStream)"
    }

    @SuppressLint("SimpleDateFormat")
    fun niceNameWithDate():String {
        val df = SimpleDateFormat("d MMM HH:mm")
        return "$courseName $courseNumber ($courseStream) ${df.format(recordingDate)}"
    }

    fun downloadRecording(context: Context, cookies: String, hq: Boolean) {
        val dir = File("${Environment.getExternalStorageDirectory()}/Download/Lecture Recordings/")
        dir.mkdirs() // creates needed dirs
        Fetch.Settings(context)
                .enableLogging(true)
                .apply()
        //val fetch = Fetch.
        val fetch = Fetch.newInstance(context)
        val request: Request
        if (hq) {
            request = Request("$urlNoExtension-slides.m4v", dir.path, "${toString()}_HQ.mp4")
            Log.i("Download", "${urlNoExtension}_HQ.mp4")
        } else {
            request = Request("$urlNoExtension.mp4", dir.path, "${toString()}.mp4")
            Log.i("Download", "$urlNoExtension.mp4")
        }
        request.addHeader("Cookie", cookies)
        downloadID = fetch.enqueue(request)
        Log.d("DownloadStatus", "ID: $downloadID")
        downloadHQ = hq
        if (downloadID != Fetch.ENQUEUE_ERROR_ID.toLong()) {
            // Download started successfully
            downloading = true
            dlError = false
            dlProgress = 0
            sendDownloadProgressNotification(context, 0)
            sendUpdate()
        } else {
            // Error, so delete the file if any.
            File("${Environment.getExternalStorageDirectory()}/Download/Lecture Recordings/${toString()}.mp4").delete()
            File("${Environment.getExternalStorageDirectory()}/Download/Lecture Recordings/${toString()}_HQ.mp4").delete()
            fetch.remove(downloadID)
            dlError = true
            dlProgress = 0
            sendDownloadErrorNotification(context)
            sendUpdate()
        }
        fetch.addFetchListener(object : FetchListener {
            override fun onUpdate(id: Long, status: Int, progress: Int, downloadedBytes: Long, fileSize: Long, error: Int) {
                Log.d("DownloadStatus", "ID: $id, Status: $status, Progress: $progress, Error: $error, FileSize: $fileSize")
                if (id == downloadID) {
                    if (status == Fetch.STATUS_ERROR) {
                        downloading = false
                        // Delete error file if it exists
                        File("${Environment.getExternalStorageDirectory()}/Download/Lecture Recordings/${toString()}.mp4").delete()
                        File("${Environment.getExternalStorageDirectory()}/Download/Lecture Recordings/${toString()}_HQ.mp4").delete()
                        dlProgress = progress
                        dlError = true
                        sendDownloadErrorNotification(context)
                        fetch.remove(downloadID)
                        sendUpdate()
                    } else if (status == Fetch.STATUS_DOWNLOADING) {
                        sendUpdate()
                        dlProgress = progress
                        dlError = false
                        sendDownloadProgressNotification(context, progress)
                    } else if (status == Fetch.STATUS_DONE) {
                        downloaded = true
                        downloading = false
                        dlError = false
                        sendDownloadCompleteNotification(context)
                        sendUpdate()
                    }
                }
            }
        })
    }

    private fun sendDownloadCompleteNotification(context: Context) {
        val notifBuilder = NotificationCompat.Builder(context, "downloads")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle("Download Complete")
                .setContentText(niceNameWithDate())

        notifBuilder.setContentIntent(getRecordingActivityPendingIntent(context))
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        mNotificationManager.notify(hashCode(), notifBuilder.build())
    }

    private fun sendDownloadErrorNotification(context: Context) {
        val notifBuilder = NotificationCompat.Builder(context, "downloads")
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle("Download Failed")
                .setContentText(niceNameWithDate())

        notifBuilder.setContentIntent(getRecordingActivityPendingIntent(context))
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        mNotificationManager.notify(hashCode(), notifBuilder.build())
    }

    private fun sendDownloadProgressNotification(context: Context, progress: Int) {
        val notifBuilder: NotificationCompat.Builder
        if (progress > 0) {
            notifBuilder = NotificationCompat.Builder(context, "downloads")
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setContentTitle("Downloading")
                    .setProgress(100, progress, false)
                    .setContentText(niceNameWithDate())
        } else {
            // Still pending
            notifBuilder = NotificationCompat.Builder(context, "downloads")
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setContentTitle("Download pending")
                    .setProgress(100, progress, true)
                    .setContentText(niceNameWithDate())
        }

        notifBuilder.setContentIntent(getRecordingActivityPendingIntent(context))
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        mNotificationManager.notify(hashCode(), notifBuilder.build())
    }

    private fun getRecordingActivityPendingIntent(context: Context) : PendingIntent {
        // Creates an explicit intent for an Activity in your app
        val resultIntent = Intent(context, RecordingViewActivity::class.java)
        resultIntent.putExtra(RECORDING_ID, RecordingStore.recordings.indexOf(this))
        resultIntent.putExtra(RECORDING_FROMSORTED, false)

        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addParentStack(RecordingViewActivity::class.java)
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        return resultPendingIntent
    }
}
