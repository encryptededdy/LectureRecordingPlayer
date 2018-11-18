package nz.zhang.lecturerecordingplayer.recordings

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(entities = arrayOf(Recording::class), version = 1)
abstract class RecordingDatabase : RoomDatabase() {

    abstract fun recordingDAO(): RecordingDAO

    // Singleton logic
    companion object {
        private var INSTANCE: RecordingDatabase? = null
        fun getInstance(context: Context): RecordingDatabase? {
            if (INSTANCE == null) {
                synchronized(RecordingDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext, RecordingDatabase::class.java, "recording_database").build()
                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }

    }
}