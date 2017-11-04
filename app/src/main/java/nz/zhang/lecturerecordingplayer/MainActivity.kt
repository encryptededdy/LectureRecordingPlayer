package nz.zhang.lecturerecordingplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.ContextCompat.startActivity
import android.view.View
import com.tonyodev.fetch.Fetch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Remove all Fetch requests in db
        Fetch.newInstance(applicationContext).removeRequests()

        // Ask for storage perms
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        Array<String>(1){Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
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
