package nz.zhang.lecturerecordingplayer

import android.annotation.SuppressLint
import android.os.Bundle
import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.webkit.WebViewClient

import kotlinx.android.synthetic.main.activity_recording_view.*
import nz.zhang.lecturerecordingplayer.recordings.Recording
import nz.zhang.lecturerecordingplayer.recordings.RecordingStore
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import android.widget.Toast
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.webkit.CookieManager
import android.webkit.DownloadListener
import android.webkit.WebView
import kotlinx.android.synthetic.main.activity_canvas_browser.*
import com.tonyodev.fetch.Fetch
import com.tonyodev.fetch.request.Request
import android.view.Gravity
import com.tonyodev.fetch.listener.FetchListener
import nz.zhang.lecturerecordingplayer.R.id.downloadWebView


class RecordingViewActivity : AppCompatActivity() {

    companion object {
        const val RECORDING_ID = "recording_id"
    }

    lateinit var recording: Recording
    var cookies = ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recording_view)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Start up the webview
        downloadWebView.webViewClient = object : WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                cookies = CookieManager.getInstance().getCookie(url)
                if (webview.url.contains("mediastore.auckland.ac.nz")) {
                    // OK we're authenticated - let's start the download
                } else {
                    // Oh no, we've been redirected - need to get the user to authenticate
                }
                super.onPageFinished(view, url)
            }
        }
        downloadWebView.settings.javaScriptEnabled = true
        populateRecording()
    }


    @SuppressLint("SetTextI18n") // Course names shouldn't need to be translated
    fun populateRecording() {
        recording = RecordingStore.recordings.get(intent.getIntExtra(RECORDING_ID, 0))
        courseName.text = "${recording.courseName} ${recording.courseNumber} ${recording.courseStream}"
        val df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())
        courseTime.text = df.format(recording.recordingDate)
    }

    fun downloadRecording(view: View) {
        Fetch.Settings(applicationContext)
                .enableLogging(true)
                .apply()
        val fetch = Fetch.newInstance(this)
        val request = Request("${recording.urlNoExtension}.mp4", "${recording.toString()}.mp4")
        request.addHeader("Cookie", cookies)
        fetch.addFetchListener(object : FetchListener {
            override fun onUpdate(id: Long, status: Int, progress: Int, downloadedBytes: Long, fileSize: Long, error: Int) {
                Log.d("DownloadStatus", "Status: $status, Progress: $progress, Error: $error, FileSize: $fileSize")

            }
        });
        val downloadId = fetch.enqueue(request)
        if (downloadId != Fetch.ENQUEUE_ERROR_ID.toLong()) {
            Toast.makeText(applicationContext, "Download started...", Toast.LENGTH_LONG).show()
        }
        Toast.makeText(applicationContext, "Downloading Recording...", Toast.LENGTH_LONG).show()
        Log.d("Download", "${recording.urlNoExtension}.mp4")
    }

    fun loadRecording(view: View) {
        downloadWebView.loadUrl("${recording.urlNoExtension}.preview")
    }

}
