package nz.zhang.lecturerecordingplayer.recordings

import android.annotation.SuppressLint
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

const val COURSE_REGEX: String = "(?<=(\\/))[A-Z]{4,8}\\d{3}\\w{4,5}(?=(\\/))"
const val TIME_REGEX: String = "(?<=(\\/))\\d{12}(?=(\\.))"

class Recording(val url: String) {
    var courseName: String = ""
    var recordingDate: Date = Date()

    init {
        // Example: 201707281200
        val dateFormat = SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault())
        // Read the course name
        val name = COURSE_REGEX.toRegex().find(url)?.value
        val time = TIME_REGEX.toRegex().find(url)?.value

        if (name != null) {
            Log.d("foundName", name)
            courseName = name
        }

        if (time != null) {
            recordingDate = dateFormat.parse(time)
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun toString(): String {
        val df = SimpleDateFormat("YYYY-MM-dd HH:mm")
        return "$courseName: ${df.format(recordingDate)}"
    }

}
