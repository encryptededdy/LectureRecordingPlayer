package nz.zhang.lecturerecordingplayer.recordings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import nz.zhang.lecturerecordingplayer.R
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_recording_view.*
import kotlinx.android.synthetic.main.item_recording.view.*
import nz.zhang.lecturerecordingplayer.R.id.courseName
import nz.zhang.lecturerecordingplayer.R.id.courseTime
import nz.zhang.lecturerecordingplayer.RecordingViewActivity
import java.text.DateFormat
import java.util.*


class RecordingAdapter(context: Context, recordingsList: List<Recording>) : RecyclerView.Adapter<RecordingAdapter.ViewHolder>() {

    val recordings = recordingsList

    lateinit var courseName: TextView
    lateinit var courseTime: TextView
    lateinit var background: ConstraintLayout

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            courseName = itemView.courseName
            courseTime = itemView.courseTime
            background = itemView.backLayout
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)

        // Inflate the custom layout
        val recordingView = inflater.inflate(R.layout.item_recording, parent, false)
        // Return a new holder instance
        return ViewHolder(recordingView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: RecordingAdapter.ViewHolder, position: Int) {
        // Get the data model based on position
        val recording = recordings.get(position)
        background.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val recordingIntent = Intent(background.context, RecordingViewActivity::class.java)
                recordingIntent.putExtra(RecordingViewActivity.RECORDING_ID, position)
                startActivity(background.context, recordingIntent, recordingIntent.extras)
            }
        })
        // Set item views
        courseName.text = "${recording.courseName} ${recording.courseNumber}"
        val df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
        courseTime.text = df.format(recording.recordingDate)
    }

    override fun getItemCount(): Int {
        return recordings.size
    }
}