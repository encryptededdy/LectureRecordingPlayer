package nz.zhang.lecturerecordingplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.afollestad.materialdialogs.MaterialDialog
import com.tonyodev.fetch.Fetch
import kotlinx.android.synthetic.main.activity_main.*
import nz.zhang.lecturerecordingplayer.recordings.RecordingStore


const val PREFS_NAME = "RecordingStorage"
const val VERSION = "Beta 1.2"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Remove all Fetch requests in db
        Fetch.newInstance(applicationContext).removeRequests()

        // Ask for storage perms
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        Array<String>(1){Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0)
        }
        RecordingStore.sharedPrefs = getSharedPreferences(PREFS_NAME, 0)
        RecordingStore.loadList()
        versionText.text = VERSION
    }

    fun showCanvas(view: View) {
        val canvasIntent = Intent(this, CanvasBrowser::class.java)
        startActivity(canvasIntent)
    }

    fun showRecordingList(view: View) {
        val recordingIntent = Intent(this, RecordingListActivity::class.java)
        startActivity(recordingIntent)
    }

    fun showCanvasScraper(view: View) {
        val scraperIntent = Intent(this, CanvasScraperActivity::class.java)
        startActivity(scraperIntent)
    }

    fun showOSSLicenses(view: View) {
        MaterialDialog.Builder(this)
                .title(R.string.open_source_licenses)
                .content("Libraries used in this software are licensed under various licenses;\n\nRetrofit2: Copyright 2013 Square, Inc., Apache License 2.0\nFetch: Copyright 2017 Tonyo Francis., Apache License 2.0\nMaterial-Dialogs: MIT License\nOKHttp3: Apache License 2.0\n\nLicensed under the Apache License, Version 2.0 (the \"License\");\n" +
                        "you may not use this file except in compliance with the License.\n" +
                        "You may obtain a copy of the License at\n" +
                        "\n" +
                        "   http://www.apache.org/licenses/LICENSE-2.0\n" +
                        "\n" +
                        "Unless required by applicable law or agreed to in writing, software\n" +
                        "distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                        "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                        "See the License for the specific language governing permissions and\n" +
                        "limitations under the License.\n\n" +
                        "The MIT License\n" +
                        "Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the \"Software\"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:\n" +
                        "\n" +
                        "The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.\n" +
                        "\n" +
                        "THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.\n")
                .positiveText(getString(R.string.ok))
                .show()
    }

}
