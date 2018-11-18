package nz.zhang.lecturerecordingplayer.recordings

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query

@Dao
interface RecordingDAO {
    @Query("SELECT * FROM RECORDINGS")
    fun getAllFromDB() : List<Recording>

    @Insert(onConflict = OnConflictStrategy.FAIL)
    fun add(recording: Recording)
}