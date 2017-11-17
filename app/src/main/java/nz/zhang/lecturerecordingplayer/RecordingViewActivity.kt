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
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.activity_recording_view.*
import nz.zhang.lecturerecordingplayer.recordings.Recording
import nz.zhang.lecturerecordingplayer.recordings.RecordingStatusListener
import nz.zhang.lecturerecordingplayer.recordings.RecordingStore
import java.text.DateFormat
import java.util.*


class RecordingViewActivity : AppCompatActivity() {

    companion object {
        const val RECORDING_ID = "recording_id"
        const val RECORDING_FROMSORTED = "recording_fromsorted"
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
                when(view?.title) {
                    "Media preview - The University of Auckland" -> {
                        // OK we're authenticated - let's start the download
                        downloadWebView.visibility = View.GONE
                        cookies = CookieManager.getInstance().getCookie(url)
                        recording.downloadRecording(applicationContext, cookies, hqCheckBox.isChecked)
                    }
                    "User dashboard" -> // Loaded CANVAS! Now let's load the media page
                        //downloadWebView.visibility = View.GONE
                        downloadWebView.loadUrl("${recording.urlNoExtension}.preview")
                    else -> {
                        // Oh no, we've been redirected - need to get the user to authenticate
                        downloadWebView.visibility = View.VISIBLE
                        Toast.makeText(applicationContext, "Please log in to download", Toast.LENGTH_LONG).show()
                        Log.d("LoadedPage", view?.url)
                    }
                }
                super.onPageFinished(view, url)
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                Toast.makeText(applicationContext, "SSL Error", Toast.LENGTH_SHORT).show()
                super.onReceivedSslError(view, handler, error)
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
        if (!intent.getBooleanExtra(RECORDING_FROMSORTED, true)) {
            // If we the ID is from the unfiltered list (useful for direct referencing)
            recording = RecordingStore.recordings.toList()[intent.getIntExtra(RECORDING_ID, 0)]
        } else {
            recording = RecordingStore.filteredRecordings.toList()[intent.getIntExtra(RECORDING_ID, 0)]
        }
        courseName.text = "${recording.courseName} ${recording.courseNumber} ${recording.courseStream}"
        val df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())
        courseTime.text = df.format(recording.recordingDate)

        // Listener for download status
        recording.addListener(object : RecordingStatusListener {
            override fun update(downloading: Boolean, downloaded: Boolean, progress: Int, error: Boolean) {
                when {
                    error -> {
                        Toast.makeText(applicationContext, getString(R.string.download_error), Toast.LENGTH_LONG).show()
                        downloadButton.text = getString(R.string.error)
                        downloadButton.isEnabled = true
                    }
                    downloading -> {
                        downloadButton.text = getString(R.string.downloading)
                        downloadButton.isEnabled = false
                        hqCheckBox.isEnabled = false
                        progressBar.progress = progress
                    }
                    downloaded -> {
                        downloadButton.text = getString(R.string.delete)
                        hqCheckBox.isChecked = recording.downloadHQ
                        hqCheckBox.isEnabled = false
                        progressBar.progress = 0
                        downloadButton.isEnabled = true
                        playButton.isEnabled = true
                    }
                    else -> {
                        downloadButton.text = getString(R.string.download)
                        progressBar.progress = 0
                        hqCheckBox.isEnabled = true
                        downloadButton.isEnabled = true
                        playButton.isEnabled = false
                    }
                }
            }
        })
    }

    fun loadRecording(view: View) {
        if (recording.downloaded) {
            // Delete
            MaterialDialog.Builder(this)
                    .title(getString(R.string.delete_title))
                    .content(getString(R.string.delete_body))
                    .icon(getDrawable(R.drawable.ic_delete_white_24dp))
                    .positiveText(R.string.delete)
                    .onPositive {_, _ -> recording.delete()}
                    .negativeText(android.R.string.no)
                    .show()
        } else {
            // Download
            downloadButton.text = getString(R.string.starting)
            downloadButton.isEnabled = false
            Log.d("WebSource", "Loading: ${recording.urlNoExtension}.preview")
            downloadWebView.loadUrl("https://canvas.auckland.ac.nz/")
        }
    }

    fun playRecording(view: View) {
        val intent = Intent(Intent.ACTION_VIEW)
        if (recording.downloadHQ) {
            intent.setDataAndType(FileProvider.getUriForFile(this, "${applicationContext.packageName}.nz.zhang.lecturerecordingplayer.playbackprovider", recording.getFileHQ()), "video/mp4")
        } else {
            intent.setDataAndType(FileProvider.getUriForFile(this, "${applicationContext.packageName}.nz.zhang.lecturerecordingplayer.playbackprovider", recording.getFile()), "video/mp4")
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        this.startActivity(intent)
    }

}
