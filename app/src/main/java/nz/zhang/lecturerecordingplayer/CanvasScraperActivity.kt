package nz.zhang.lecturerecordingplayer

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.activity_canvas_scraper.*
import nz.zhang.lecturerecordingplayer.canvasscraper.CanvasScraper
import nz.zhang.lecturerecordingplayer.canvasscraper.ScraperCourseListListener
import nz.zhang.lecturerecordingplayer.canvasscraper.ScraperListener
import nz.zhang.lecturerecordingplayer.recordings.Recording
import nz.zhang.lecturerecordingplayer.recordings.RecordingStore
import nz.zhang.lecturerecordingplayer.recordings.sync.RecordingSync

const val SESSIONCOOKIE_REGEX = "canvas_session=.*?(?=;)"

class CanvasScraperActivity : AppCompatActivity() {

    // build the listener to the scraper
    val listener = object : ScraperListener {
        @SuppressLint("SetTextI18n")
        override fun update(recording: Recording) {
            if (RecordingStore.add(recording)) {
                RecordingSync.uploadRecording(recording)
            }
            runOnUiThread { foundNotif.text = "Found: ${recording.niceNameWithDate()}" }
        }

        override fun complete() {
            // display completion things
            runOnUiThread { onBackPressed() }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canvas_scraper)
        loginWebView.visibility = View.GONE
        loginWebView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                when (view?.title) {
                    "User dashboard" -> {
                        // OK we're authenticated - let's start the scrape and hide the webview
                        loginWebView.visibility = View.GONE
                        val cookies = CookieManager.getInstance().getCookie(url)
                        val sessionID = SESSIONCOOKIE_REGEX.toRegex().find(cookies)?.value
                        if (sessionID != null) {
                            // have the sessionID, start scraping!
                            statusNotif.text = getText(R.string.scrapeText)
                            startScraper(sessionID)
                        } else {
                            Toast.makeText(applicationContext, getString(R.string.tokenError), Toast.LENGTH_LONG).show()
                        }
                    }
                    else -> {
                        loginWebView.visibility = View.VISIBLE
                        // Oh no, we've been redirected - need to get the user to authenticate
                        Toast.makeText(applicationContext, "Please authenticate", Toast.LENGTH_LONG).show()
                        Log.d("LoadedPage", view?.url)
                    }
                }
                super.onPageFinished(view, url)
            }
        }
        loginWebView.settings.javaScriptEnabled = true
        if (CanvasScraper.isRunning) {
            statusNotif.text = getText(R.string.scrapeText)
            CanvasScraper.setListener(listener)
        } else {
            loginWebView.loadUrl("https://canvas.auckland.ac.nz/")
        }
    }

    private fun startScraper(sessionID: String) {
        val parentContext = this
        val scrapeThread = Thread(Runnable {
            val idListener = object : ScraperCourseListListener {
                @SuppressLint("SetTextI18n")
                override fun update(ids: List<Int>, names: List<String>, scraper: CanvasScraper) {
                    // build and show selection dialog
                    runOnUiThread {
                        try {
                            MaterialDialog.Builder(parentContext)
                                    .title(getString(R.string.scrape_title))
                                    .content(getString(R.string.scrape_desc))
                                    .items(names)
                                    .itemsCallbackMultiChoice(null) { dialog, which, text ->
                                        val selectedCourseIDs = ArrayList<Int>()
                                        which.forEach { pos -> selectedCourseIDs.add(ids[pos]) }
                                        System.out.println(selectedCourseIDs)
                                        // now scrape for the data
                                        Thread(Runnable {
                                            CanvasScraper.run(selectedCourseIDs, listener)
                                        }).start()
                                        true
                                    }
                                    .positiveText(getString(R.string.start))
                                    .show()
                        } catch (e: Exception) {
                            // Oops, couldn't show the dialog. So do nothing.
                        }
                    }
                }
            }

            try {
                // run the scraper
                CanvasScraper.getCourseData(idListener, sessionID)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
        scrapeThread.start()
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
}
