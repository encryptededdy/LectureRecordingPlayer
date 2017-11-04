package nz.zhang.lecturerecordingplayer.recordings

import com.google.gson.Gson
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.google.gson.reflect.TypeToken

const val STORAGE_KEY = "recordings"

object RecordingStore {
    var recordings: ArrayList<Recording> = ArrayList()
    lateinit var sharedPrefs: SharedPreferences

    fun add(recording: Recording): Boolean {
        if (!recordings.contains(recording)) {
            recordings.add(recording)
            // write to prefs
            val editor = sharedPrefs.edit()
            editor.putString(STORAGE_KEY, Gson().toJson(recordings))
            editor.apply()
            recordings.sort() // sort by date
            return true
        } else {
            return false
        }
    }

    fun loadList() {
        val recordingListType = object : TypeToken<ArrayList<Recording>>() {}.type
        val storedRecordings: ArrayList<Recording>? = Gson().fromJson(sharedPrefs.getString(STORAGE_KEY, ""), recordingListType)
        if (storedRecordings != null) {
            recordings = storedRecordings
            for (recording : Recording in recordings)
                recording.checkFS()
        }
        // Otherwise, just stick with the empty array
    }
}