package nz.zhang.lecturerecordingplayer.recordings

interface RecordingStatusListener {
    fun update(downloading: Boolean, downloaded: Boolean, progress: Int, error: Boolean)
}