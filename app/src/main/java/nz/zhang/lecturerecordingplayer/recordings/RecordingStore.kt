package nz.zhang.lecturerecordingplayer.recordings

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.ArrayList

const val STORAGE_KEY = "recordings"

object RecordingStore {
    var recordings: TreeSet<Recording> = TreeSet()
    var filteredRecordings: ArrayList<Recording> = ArrayList()

    lateinit var sharedPrefs: SharedPreferences

    fun loadList() {
        val recordingListType = object : TypeToken<TreeSet<Recording>>() {}.type
        System.out.println(sharedPrefs.getString(STORAGE_KEY, ""))
        val storedRecordings: TreeSet<Recording>? = Gson().fromJson(sharedPrefs.getString(STORAGE_KEY, ""), recordingListType)
        if (storedRecordings != null) {
            recordings = storedRecordings
            for (recording : Recording in recordings)
                recording.checkFS()
        }
        // Otherwise, just stick with the empty array
    }

    fun loadAllRecordings() {
        filteredRecordings = if (!recordings.isEmpty()){
            recordings.descendingSet().toList() as ArrayList
        } else {
            ArrayList()
        }
    }

    // Gets courses
    fun courseList() : List<Course> {
        val uniqueCourses = HashSet<Course>()
        recordings.forEach { recording: Recording ->
            uniqueCourses.add(Course(recording.courseName + recording.courseNumber + recording.courseStream, recording.semesterNumber, recording.niceName()))}
        return uniqueCourses.toList()
    }
}