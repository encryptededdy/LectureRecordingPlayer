package nz.zhang.lecturerecordingplayer

import android.annotation.SuppressLint
import android.os.Bundle
import android.app.Activity
import android.support.v7.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_recording_view.*
import nz.zhang.lecturerecordingplayer.recordings.Recording
import nz.zhang.lecturerecordingplayer.recordings.RecordingStore
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class RecordingViewActivity : AppCompatActivity() {

    companion object {
        const val RECORDING_ID = "recording_id"
    }

    lateinit var recording: Recording

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recording_view)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        populateRecording()
    }

    @SuppressLint("SetTextI18n") // Course names shouldn't need to be translated
    fun populateRecording() {
        recording = RecordingStore.recordings.get(intent.getIntExtra(RECORDING_ID, 0))
        courseName.text = "${recording.courseName} ${recording.courseNumber} ${recording.courseStream}"
        val df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
        courseTime.text = df.format(recording.recordingDate)
    }

}
