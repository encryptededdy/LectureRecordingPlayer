package nz.zhang.lecturerecordingplayer.recordings.sync

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Edward Zhang on 7/11/2017.
 */
interface CanvasVideoEnhancerService {
    @GET("api/v1/playlist")
    fun getPlaylist(@Query("course")course:String, @Query("semester_code")semesterCode:String):Call<List<CanvasVideoEnhancerRecording>>
}