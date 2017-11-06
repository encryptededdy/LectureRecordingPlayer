package nz.zhang.lecturerecordingplayer

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_recording_list.*
import nz.zhang.lecturerecordingplayer.recordings.RecordingAdapter
import nz.zhang.lecturerecordingplayer.recordings.RecordingStore


class RecordingListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recording_list)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val adapter = RecordingAdapter(this, RecordingStore.recordings)
        recordingListRecycler.adapter = adapter
        recordingListRecycler.layoutManager = LinearLayoutManager(this)
    }


    // Handle custom menu bar
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.recording_list_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // handle menu button activities
    override fun onOptionsItemSelected(item: MenuItem) : Boolean {
        return when {
            item.itemId == R.id.sync_recordings -> {
                // sync recordings with server
                AlertDialog.Builder(this)
                        .setTitle(getString(R.string.sync_title))
                        .setMessage(getString(R.string.sync_body))
                        .setIcon(R.drawable.ic_sync_white_24dp)
                        .setPositiveButton(getString(R.string.sync)) { p0, p1 -> run {
                            // todo: the actual sync
                        } }
                        .setNegativeButton(android.R.string.no, null)
                        .show()
                true
            }
            item.itemId == android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}
