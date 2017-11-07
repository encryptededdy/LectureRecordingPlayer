package nz.zhang.lecturerecordingplayer.recordings.sync

import nz.zhang.lecturerecordingplayer.recordings.Recording
import java.net.MalformedURLException

const val SUFFIX_REGEX = "LT\\d{6}(?:\\.REV\\d)?"

class CanvasVideoEnhancerRecording (
        val video_id:Int,
        val year:Int,
        val month:Int,
        val day:Int,
        val hour:Int,
        val minute:Int,
        val course:String,
        val course_year:Int,
        val semester_code:Int,
        val infix:String,
        val prefix:String,
        val suffix:String
) {
    // check if the suffix is valid
    fun isValid():Boolean {
        return SUFFIX_REGEX.toRegex().find(suffix)?.value != null
    }

    fun toRecording():Recording {
        // clean suffix
        val cleanSuffix = SUFFIX_REGEX.toRegex().find(suffix)?.value
        if (cleanSuffix != null) {
            // build the ugly url
            val url = "https://mediastore.auckland.ac.nz/$course_year/$semester_code/$course/$infix/$prefix$year${String.format("%02d%02d%02d%02d", month, day, hour, minute)}.$cleanSuffix"
            System.out.println("Converted URL: $url")
            return Recording(url)
        } else {
            throw MalformedURLException("URL has invalid suffix")
        }
    }
}