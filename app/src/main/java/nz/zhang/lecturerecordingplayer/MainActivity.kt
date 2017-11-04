package nz.zhang.lecturerecordingplayer

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.tonyodev.fetch.Fetch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Remove all Fetch requests in db
        Fetch.newInstance(applicationContext).removeRequests()
    }

    fun showCanvas(view: View) {
        val canvasIntent = Intent(this, CanvasBrowser::class.java)
        startActivity(canvasIntent)
    }

    fun showRecordingList(view: View) {
        val recordingIntent = Intent(this, RecordingListActivity::class.java)
        startActivity(recordingIntent)
    }
}
