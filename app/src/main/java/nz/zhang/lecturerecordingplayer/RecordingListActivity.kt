package nz.zhang.lecturerecordingplayer

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_recording_list.*
import nz.zhang.lecturerecordingplayer.recordings.Recording
import nz.zhang.lecturerecordingplayer.recordings.RecordingStore
import java.util.*
import android.widget.Toast
import android.widget.AdapterView.OnItemClickListener
import nz.zhang.lecturerecordingplayer.recordings.RecordingAdapter


class RecordingListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recording_list)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val adapter = RecordingAdapter(this, RecordingStore.recordings)
        recordingListRecycler.adapter = adapter
        recordingListRecycler.layoutManager = LinearLayoutManager(this)
    }
}
