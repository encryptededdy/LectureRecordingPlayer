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
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.item_recording.view.*
import nz.zhang.lecturerecordingplayer.R
import nz.zhang.lecturerecordingplayer.RecordingViewActivity
import java.text.DateFormat
import java.util.*


class RecordingAdapter(context: Context, var recordings: List<Recording>) : RecyclerView.Adapter<RecordingAdapter.ViewHolder>() {

    init {
        setHasStableIds(true)

        // Add listener for status updates to the recording
        recordings.forEachIndexed {index:Int, recording:Recording ->
            recording.addListener(object : RecordingStatusListener {
                override fun update(downloading: Boolean, downloaded: Boolean, progress: Int, error: Boolean) {
                    val nothing = Unit // hacky way to prevent animation on update
                    notifyItemChanged(index, nothing)
                }
            })
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var courseName: TextView = itemView.courseName
        var courseTime: TextView = itemView.courseTime
        var background: ConstraintLayout = itemView.backLayout
        var downloadedIcon: ImageView = itemView.downloadedIcon
        var downloadProgress: TextView = itemView.downloadProgress
        var hqIcon: ImageView = itemView.hqIcon
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
        viewHolder.background.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val recordingIntent = Intent(viewHolder.background.context, RecordingViewActivity::class.java)
                recordingIntent.putExtra(RecordingViewActivity.RECORDING_ID, position)
                startActivity(viewHolder.background.context, recordingIntent, recordingIntent.extras)
            }
        })

        when {
            recording.dlError -> {
                // just hide everything
                viewHolder.downloadProgress.visibility = View.INVISIBLE
                viewHolder.downloadedIcon.visibility = View.INVISIBLE
            }
            recording.downloading -> {
                viewHolder.downloadedIcon.visibility = View.INVISIBLE
                viewHolder.downloadProgress.visibility = View.VISIBLE
                viewHolder.downloadProgress.text = "${recording.dlProgress}%"
            }
            recording.downloaded -> {
                viewHolder.downloadedIcon.visibility = View.VISIBLE
                viewHolder.downloadProgress.visibility = View.INVISIBLE
            }
            else -> {
                viewHolder.downloadProgress.visibility = View.INVISIBLE
                viewHolder.downloadedIcon.visibility = View.INVISIBLE
            }
        }

        if (recording.downloadHQ) {
            viewHolder.hqIcon.visibility = View.VISIBLE
        } else {
            viewHolder.hqIcon.visibility = View.INVISIBLE
        }

        // Set item views
        viewHolder.courseName.text = recording.niceName()
        val df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())
        viewHolder.courseTime.text = df.format(recording.recordingDate)
    }

    override fun getItemCount(): Int {
        return recordings.size
    }

    fun update(new:List<Recording>) {
        recordings = new
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}