package nz.zhang.lecturerecordingplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.net.http.SslError
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_recording_view.*
import nz.zhang.lecturerecordingplayer.recordings.Recording
import nz.zhang.lecturerecordingplayer.recordings.RecordingStatusListener
import nz.zhang.lecturerecordingplayer.recordings.RecordingStore
import java.text.DateFormat
import java.util.*


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
        downloadWebView.visibility = View.GONE
        downloadWebView.webViewClient = object : WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                if (view?.title.equals("Media preview - The University of Auckland")) {
                    // OK we're authenticated - let's start the download
                    downloadWebView.visibility = View.GONE
                    cookies = CookieManager.getInstance().getCookie(url)
                    recording.downloadRecording(applicationContext, cookies)
                } else if (view?.title.equals("User dashboard")) {
                    // Loaded CANVAS! Now let's load the media page
                    //downloadWebView.visibility = View.GONE
                    downloadWebView.loadUrl("${recording.urlNoExtension}.preview")
                } else {
                    // Oh no, we've been redirected - need to get the user to authenticate
                    downloadWebView.visibility = View.VISIBLE
                    Toast.makeText(applicationContext, "Please log in to download", Toast.LENGTH_LONG).show()
                    Log.d("LoadedPage", view?.url)
                }
                super.onPageFinished(view, url)
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                Toast.makeText(applicationContext, "SSL Error, loading CANVAS now", Toast.LENGTH_SHORT).show()
                super.onReceivedSslError(view, handler, error)
                //downloadWebView.loadUrl("https://canvas.auckland.ac.nz/")
            }
        }
        downloadWebView.settings.javaScriptEnabled = true
        populateRecording()
    }

    // Handle back button functionality
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return if (item?.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }


    @SuppressLint("SetTextI18n") // Course names shouldn't need to be translated
    fun populateRecording() {
        recording = RecordingStore.recordings.get(intent.getIntExtra(RECORDING_ID, 0))
        courseName.text = "${recording.courseName} ${recording.courseNumber} ${recording.courseStream}"
        val df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())
        courseTime.text = df.format(recording.recordingDate)
        // Set button statuses
        if (recording.downloading) {
            downloadButton.text = getString(R.string.downloading)
            downloadButton.isEnabled = false
        }
        if (recording.downloaded) {
            downloadButton.text = getString(R.string.downloaded)
            downloadButton.isEnabled = false
            playButton.isEnabled = true
        }

        // Listener for download status
        recording.addListener(object : RecordingStatusListener {
            override fun update(downloading: Boolean, downloaded: Boolean, progress: Int, error: Boolean) {
                if (error) {
                    Toast.makeText(applicationContext, "Download error. You may need to authenticate", Toast.LENGTH_LONG).show()
                    downloadButton.text = getString(R.string.error)
                    downloadButton.isEnabled = true
                } else if (recording.downloading) {
                    downloadButton.text = getString(R.string.downloading)
                    downloadButton.isEnabled = false
                    progressBar.progress = progress
                } else if (recording.downloaded) {
                    downloadButton.text = getString(R.string.downloaded)
                    progressBar.progress = 0
                    downloadButton.isEnabled = false
                    playButton.isEnabled = true
                }
            }
        })
    }

    fun loadRecording(view: View) {
        downloadButton.text = getString(R.string.starting)
        downloadButton.isEnabled = false
        Log.d("WebSource", "Loading: ${recording.urlNoExtension}.preview")
        downloadWebView.loadUrl("https://canvas.auckland.ac.nz/")
    }

    fun playRecording(view: View) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(FileProvider.getUriForFile(this, "${applicationContext.packageName}.nz.zhang.lecturerecordingplayer.playbackprovider", recording.getFile()), "video/mp4")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        applicationContext.startActivity(intent)
    }

}
