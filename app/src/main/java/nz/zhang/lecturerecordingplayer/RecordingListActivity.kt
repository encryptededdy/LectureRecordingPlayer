package nz.zhang.lecturerecordingplayer

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_recording_list.*
import nz.zhang.lecturerecordingplayer.recordings.Recording
import nz.zhang.lecturerecordingplayer.recordings.RecordingStore
import java.util.*
import android.widget.Toast
import android.widget.AdapterView.OnItemClickListener



class RecordingListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recording_list)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val adapter = ArrayAdapter<Recording>(this, android.R.layout.simple_list_item_1, RecordingStore.recordings)

        recordingListView.adapter = adapter

        recordingListView.setOnItemClickListener({ _, _, position, _ ->
            recordingClick(position)
        })
    }

    fun recordingClick(position: Int) {
        val recordingIntent = Intent(this, RecordingViewActivity::class.java)
        recordingIntent.putExtra(RecordingViewActivity.RECORDING_ID, position)
        startActivity(recordingIntent)
    }
}
