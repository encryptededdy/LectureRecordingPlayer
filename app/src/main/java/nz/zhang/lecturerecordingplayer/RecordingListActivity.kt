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
import nz.zhang.lecturerecordingplayer.recordings.sync.RecordingSync
import android.app.ProgressDialog
import nz.zhang.lecturerecordingplayer.recordings.sync.SyncCallback


class RecordingListActivity : AppCompatActivity() {

    lateinit var adapter:RecordingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recording_list)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = RecordingAdapter(this, RecordingStore.recordings.descendingSet().toList()) // descending for latest first
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
        val parentContext = this
        return when (item.itemId) {
            R.id.sync_recordings -> {
                // sync recordings with server
                AlertDialog.Builder(this)
                        .setTitle(getString(R.string.sync_title))
                        .setMessage(getString(R.string.sync_body))
                        .setIcon(R.drawable.ic_sync_white_24dp)
                        .setPositiveButton(getString(R.string.sync)) { _, _ -> run {
                            val dialog = ProgressDialog.show(parentContext, "",
                                    "Syncing from server", true)
                            RecordingSync(object : SyncCallback {
                                override fun update(newRecordings: Int) {
                                    // On sync success
                                    dialog.dismiss()
                                    adapter = RecordingAdapter(parentContext, RecordingStore.recordings.descendingSet().toList())
                                    recordingListRecycler.adapter = adapter
                                    recordingListRecycler.invalidate()
                                    AlertDialog.Builder(parentContext)
                                            .setMessage("Synced $newRecordings new recordings from the server")
                                            .setCancelable(false)
                                            .setPositiveButton("OK", null)
                                            .show()
                                }
                            }).sync()
                        } }
                        .setNegativeButton(android.R.string.no, null)
                        .show()
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}
