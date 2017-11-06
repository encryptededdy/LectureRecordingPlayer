package nz.zhang.lecturerecordingplayer.recordings

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.util.Log
import com.tonyodev.fetch.Fetch
import com.tonyodev.fetch.listener.FetchListener
import com.tonyodev.fetch.request.Request
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


const val COURSENAME_REGEX: String = "(?<=(/))[A-Z]{4,8}(?=(\\d{3}\\w{4,5}/))"
const val COURSENUMBER_REGEX: String = "(?<=(/[A-Z]{4,8}))\\d{3}(?=(\\w{4,5}/))"
const val COURSESTREAM_REGEX: String = "(?<=(/[A-Z]{4,8}\\d{3}))\\w{4,5}(?=(/))"
const val SEMESTERNUMBER_REGEX: String = "(?<=(\\/))\\d{4}(?=(\\/[A-Z]{4,8}\\d{3}\\w{4,5}\\/))"

const val FILEEXTENSION_REGEX: String = "(\\.preview|\\.mp4|\\.m4v|\\.mp3|-slides\\.m4v)\$"

const val TIME_REGEX: String = "(?<=(\\/))\\d{12}(?=(\\.))"

class Recording(val url: String) : Comparable<Recording> {
    val courseName: String
    val courseNumber: String
    val courseStream: String

    val semesterNumber: String

    val recordingDate: Date

    val urlNoExtension: String

    @Transient private val statusListeners: ArrayList<RecordingStatusListener> = ArrayList()

    @Transient var downloading = false
    @Transient var downloaded = false

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
        } else {
            courseName = "Not found"; courseStream = "Not found"; urlNoExtension = "Not found"; courseNumber = "Not found"; recordingDate = Date(); semesterNumber = "0000"
        }
    }

    private constructor() : this("")

    @SuppressLint("SimpleDateFormat")
    override fun toString(): String {
        val df = SimpleDateFormat("YYYY-MM-dd_HHmm")
        return "${courseName}_${courseNumber}_${df.format(recordingDate)}"
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
            return urlNoExtension.equals(other.urlNoExtension)
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

    fun delete() {
        if (getFile().delete()) downloaded = false
        sendUpdate()
    }

    fun checkFS() {
        val file = getFile()
        if (file.isFile && file.length() > 10000) {
            Log.d("FileCheck", "${Environment.getExternalStorageDirectory()}/Download/Lecture Recordings/${toString()}.mp4 Exists")
            downloaded = true
        } else if (file.length() < 10000) {
            // Delete empty files
            file.delete()
            downloaded = false
        } else {
            downloaded = false
        }
        //Log.d("FileCheck", "${Environment.getExternalStorageDirectory()}/Download/Lecture Recordings/${toString()}.mp4 Doesn't Exist")
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
            dlError = false
            dlProgress = 0
            sendUpdate()
        } else {
            // Error, so delete the file if any.
            File("${Environment.getExternalStorageDirectory()}/Download/Lecture Recordings/${toString()}.mp4").delete()
            dlError = true
            dlProgress = 0
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
                        dlProgress = progress
                        dlError = true
                        sendUpdate()
                    } else if (status == Fetch.STATUS_DOWNLOADING) {
                        sendUpdate()
                        dlProgress = progress
                        dlError = false
                    } else if (status == Fetch.STATUS_DONE) {
                        downloaded = true
                        downloading = false
                        dlError = false
                        sendUpdate()
                    }
                }
            }
        })
        Log.d("Download", "$urlNoExtension.mp4")
    }

}
