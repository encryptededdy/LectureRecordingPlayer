package nz.zhang.lecturerecordingplayer.recordings

import android.annotation.SuppressLint
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

const val COURSENAME_REGEX: String = "(?<=(/))[A-Z]{4,8}(?=(\\d{3}\\w{4,5}/))"
const val COURSENUMBER_REGEX: String = "(?<=(/[A-Z]{4,8}))\\d{3}(?=(\\w{4,5}/))"
const val COURSESTREAM_REGEX: String = "(?<=(/[A-Z]{4,8}\\d{3}))\\w{4,5}(?=(/))"

const val TIME_REGEX: String = "(?<=(\\/))\\d{12}(?=(\\.))"

class Recording(val url: String) {
    lateinit var courseName: String
    lateinit var courseNumber: String
    lateinit var courseStream: String

    var recordingDate: Date = Date()

    init {
        // Example: 201707281200
        val dateFormat = SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault())
        // Read the course name
        val name = COURSENAME_REGEX.toRegex().find(url)?.value
        val number = COURSENUMBER_REGEX.toRegex().find(url)?.value
        val stream = COURSESTREAM_REGEX.toRegex().find(url)?.value

        val time = TIME_REGEX.toRegex().find(url)?.value

        if (name != null && number != null && stream != null && time != null) {
            Log.d("foundName", name)
            courseName = name
            courseNumber = number
            courseStream = stream
            recordingDate = dateFormat.parse(time)
        } else {
            courseName = "Not found"; courseName = "Not found"; courseStream = "Not found"
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun toString(): String {
        val df = SimpleDateFormat("YYYY-MM-dd HH:mm")
        return "$courseName $courseNumber: ${df.format(recordingDate)}"
    }

}
