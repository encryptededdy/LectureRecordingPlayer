package nz.zhang.lecturerecordingplayer.recordings

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.tonyodev.fetch.Fetch
import com.tonyodev.fetch.listener.FetchListener
import com.tonyodev.fetch.request.Request
import kotlinx.android.synthetic.main.activity_recording_view.*
import nz.zhang.lecturerecordingplayer.R
import nz.zhang.lecturerecordingplayer.R.id.downloadButton
import nz.zhang.lecturerecordingplayer.R.id.progressBar
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.os.Environment.getExternalStorageDirectory
import java.io.File


const val COURSENAME_REGEX: String = "(?<=(/))[A-Z]{4,8}(?=(\\d{3}\\w{4,5}/))"
const val COURSENUMBER_REGEX: String = "(?<=(/[A-Z]{4,8}))\\d{3}(?=(\\w{4,5}/))"
const val COURSESTREAM_REGEX: String = "(?<=(/[A-Z]{4,8}\\d{3}))\\w{4,5}(?=(/))"

const val FILEEXTENSION_REGEX: String = "(\\.preview|\\.mp4|\\.mp3|-slides\\.m4v)\$"

const val TIME_REGEX: String = "(?<=(\\/))\\d{12}(?=(\\.))"

class Recording(val url: String) {
    lateinit var courseName: String
    lateinit var courseNumber: String
    lateinit var courseStream: String

    lateinit var urlNoExtension: String

    private var statusListeners: ArrayList<RecordingStatusListener> = ArrayList()

    var downloading = false
    var downloaded = false

    var downloadID: Long = -1

    var recordingDate: Date = Date()

    init {
        // Example: 201707281200
        val dateFormat = SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault())
        // Read the course name
        val name = COURSENAME_REGEX.toRegex().find(url)?.value
        val number = COURSENUMBER_REGEX.toRegex().find(url)?.value
        val stream = COURSESTREAM_REGEX.toRegex().find(url)?.value

        val extensionless = FILEEXTENSION_REGEX.toRegex().replace(url, "")

        val time = TIME_REGEX.toRegex().find(url)?.value

        if (name != null && number != null && stream != null && time != null) {
            Log.d("foundName", name)
            courseName = name
            courseNumber = number
            courseStream = stream
            urlNoExtension = extensionless
            recordingDate = dateFormat.parse(time)
        } else {
            courseName = "Not found"; courseName = "Not found"; courseStream = "Not found"; urlNoExtension = "Not found"
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun toString(): String {
        val df = SimpleDateFormat("YYYY-MM-dd_HHmm")
        return "${courseName}_${courseNumber}_${df.format(recordingDate)}"
    }

    fun addListener(listener: RecordingStatusListener) {
        statusListeners.add(listener)
    }

    private fun sendUpdate(progress: Int, error: Boolean) {
        for (listener: RecordingStatusListener in statusListeners) listener.update(downloading, downloaded, progress, error)
    }

    fun downloadRecording(context: Context, cookies: String) {
        val dir = File("${Environment.getExternalStorageDirectory()}/Download/Lecture Recordings/")
        dir.mkdirs() // creates needed dirs
        Fetch.Settings(context)
                .enableLogging(true)
                .apply()
        val fetch = Fetch.newInstance(context)
        val request = Request("$urlNoExtension.mp4", dir.path, "${toString()}.mp4")
        request.addHeader("Cookie", cookies)
        downloadID = fetch.enqueue(request)
        Log.d("DownloadStatus", "ID: $downloadID")
        if (downloadID != Fetch.ENQUEUE_ERROR_ID.toLong()) {
            // Download started successfully
            downloading = true
            sendUpdate(0, false)
        } else {
            sendUpdate(0, true)
        }
        fetch.addFetchListener(object : FetchListener {
            override fun onUpdate(id: Long, status: Int, progress: Int, downloadedBytes: Long, fileSize: Long, error: Int) {
                Log.d("DownloadStatus", "ID: $id, Status: $status, Progress: $progress, Error: $error, FileSize: $fileSize")
                if (id == downloadID) {
                    if (status == Fetch.STATUS_ERROR) {
                        sendUpdate(progress, true)
                    } else if (status == Fetch.STATUS_DOWNLOADING) {
                        sendUpdate(progress, false)
                    } else if (status == Fetch.STATUS_DONE) {
                        downloaded = true
                        downloading = false
                        sendUpdate(100, false)
                    }
                }
            }
        });
        Log.d("Download", "$urlNoExtension.mp4")
    }

}
