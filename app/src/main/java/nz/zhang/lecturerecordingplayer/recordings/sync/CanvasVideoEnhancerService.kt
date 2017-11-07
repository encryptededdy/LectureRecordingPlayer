package nz.zhang.lecturerecordingplayer.recordings.sync

import retrofit2.Call
import retrofit2.http.*

interface CanvasVideoEnhancerService {
    @GET("api/v1/playlist")
    fun getPlaylist(@Query("course")course:String, @Query("semester_code")semesterCode:String):Call<List<CanvasVideoEnhancerRecording>>

    @Headers("Content-Type: application/json")
    @POST("video")
    fun uploadRecording(@Body()url:String):Call<Void>
}