package nz.zhang.lecturerecordingplayer

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.afollestad.materialdialogs.MaterialDialog
import kotlinx.android.synthetic.main.activity_recording_list.*
import nz.zhang.lecturerecordingplayer.recordings.RecordingAdapter
import nz.zhang.lecturerecordingplayer.recordings.RecordingStore
import nz.zhang.lecturerecordingplayer.recordings.sync.RecordingSync
import nz.zhang.lecturerecordingplayer.recordings.sync.SyncComplete


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
                MaterialDialog.Builder(this)
                        .title(getString(R.string.sync_title))
                        .content(getString(R.string.sync_body))
                        .icon(getDrawable(R.drawable.ic_sync_white_24dp))
                        .positiveText(R.string.sync)
                        .onPositive { _, _ ->
                            val progressDialog = MaterialDialog.Builder(parentContext)
                                    .title(getString(R.string.pleasewait))
                                    .content(getString(R.string.synctext))
                                    .progress(true, 0)
                                    .build()
                            progressDialog.show()
                            RecordingSync(object : SyncComplete {
                                override fun update(newRecordings: Int) {
                                    // On sync success
                                    progressDialog.dismiss()
                                    val adapter = RecordingAdapter(parentContext, RecordingStore.recordings.descendingSet().toList())
                                    recordingListRecycler.adapter = adapter
                                    recordingListRecycler.invalidate()
                                    MaterialDialog.Builder(parentContext)
                                            .content("Synced $newRecordings new recordings from the server")
                                            .cancelable(false)
                                            .positiveText("OK")
                                            .show()
                                }
                            }).sync()
                        }
                        .negativeText(android.R.string.no)
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
