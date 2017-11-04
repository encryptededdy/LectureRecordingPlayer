package nz.zhang.lecturerecordingplayer

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.webkit.WebViewClient

import kotlinx.android.synthetic.main.activity_canvas_browser.*
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import nz.zhang.lecturerecordingplayer.recordings.Recording
import nz.zhang.lecturerecordingplayer.recordings.RecordingStore
import java.util.regex.Matcher
import java.util.regex.Pattern


const val CANVAS_URL: String = "https://canvas.auckland.ac.nz/"
const val RECORDING_REGEX: String = "https:\\/\\/mediastore\\.auckland\\.ac\\.nz\\/.{0,100}\\.preview"

class CanvasBrowser : AppCompatActivity() {

    var currentPageSrc: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canvas_browser)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        loadPage()
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun loadPage() {
        webview.webViewClient = object : WebViewClient(){
            override fun onPageFinished(view: WebView?, url: String?) {
                // Inject JS to read the source of the site
                webview.loadUrl("javascript:window.HTMLOUT.processHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');");
                super.onPageFinished(view, url)
            }
        }
        webview.addJavascriptInterface(CanvasPageProcessor(), "HTMLOUT")
        webview.settings.javaScriptEnabled = true
        webview.loadUrl(CANVAS_URL)
    }

    fun readSource(view: View) {
        val m = Pattern.compile(RECORDING_REGEX)
                .matcher(currentPageSrc)
        var recordingsAdded = 0
        while (m.find()) {
            if (RecordingStore.add(Recording(m.group())))
                recordingsAdded++
            //Log.d("readURL", m.group())
        }
        if (recordingsAdded == 0) {
            Toast.makeText(this, "No lecture recordings found on this page", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Found & added $recordingsAdded new lecture recordings!", Toast.LENGTH_SHORT).show()
        }
    }

    inner class CanvasPageProcessor {
        @JavascriptInterface
        fun processHTML(html: String) {
            currentPageSrc = html
        }
    }

}
