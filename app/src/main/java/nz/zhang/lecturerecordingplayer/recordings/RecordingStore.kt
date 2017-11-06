package nz.zhang.lecturerecordingplayer.recordings

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

const val STORAGE_KEY = "recordings"

object RecordingStore {
    var recordings: TreeSet<Recording> = TreeSet()
    lateinit var sharedPrefs: SharedPreferences

    fun add(recording: Recording): Boolean {
        return if (!recordings.contains(recording)) {
            recordings.add(recording)
            // write to prefs
            val editor = sharedPrefs.edit()
            editor.putString(STORAGE_KEY, Gson().toJson(recordings))
            editor.apply()
            true
        } else {
            false
        }
    }

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
}