package nz.zhang.lecturerecordingplayer.recordings.sync

import android.util.Log
import nz.zhang.lecturerecordingplayer.recordings.Course
import nz.zhang.lecturerecordingplayer.recordings.Recording
import nz.zhang.lecturerecordingplayer.recordings.RecordingStore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


const val API_ROOT_URL = "https://canvasvideoenhancer.azurewebsites.net/"

class RecordingSync(private val listener: SyncComplete) : Callback<List<CanvasVideoEnhancerRecording>> {
    var downloadedRecordings = ArrayList<CanvasVideoEnhancerRecording>()
    var callsMade = 0
    val courses:List<Course> = RecordingStore.courseList()

    companion object {
        // Upload recording to the server
        fun uploadRecording(recording: Recording) {
            val retrofit = Retrofit.Builder()
                    .baseUrl(API_ROOT_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build()
            val cveService = retrofit.create(CanvasVideoEnhancerService::class.java)
            val urlToUpload = recording.urlNoExtension.replace("^https?:\\/\\/.+?\\/".toRegex(), "")
            val call = cveService.uploadRecording("{\"url\": \"$urlToUpload\"}")
            Log.d("UploadRec", "Uploading recording $urlToUpload")
            call.enqueue(EmptyCallback())
        }
    }

    fun sync() {
        downloadPlaylist()
    }

    private fun downloadPlaylist() {
        // create the RetroFit instance
        val retrofit = Retrofit.Builder()
                .baseUrl(API_ROOT_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        val cveService = retrofit.create(CanvasVideoEnhancerService::class.java)
        courses.forEach { course: Course ->
            System.out.println("Sending request for "+course.course)
            val call = cveService.getPlaylist(course.course, course.semesterCode)
            call.enqueue(this)
        }
    }

    // Handle Responses from downloadPlaylist calls
    override fun onResponse(call: Call<List<CanvasVideoEnhancerRecording>>, response: Response<List<CanvasVideoEnhancerRecording>>) {
        callsMade++
        val recordingList:List<CanvasVideoEnhancerRecording>? = response.body()
        if (response.isSuccessful && recordingList != null) {
            System.out.println("Got callback for "+recordingList)
            downloadedRecordings.addAll(recordingList)
        } else {
            System.out.println(response.errorBody())
        }

        if (callsMade == courses.size) {
            System.out.println("Received all callbacks!")
            // All playlists downloaded! Now convert to recording and add them
            var newRecordings = 0
            downloadedRecordings.forEach { cveRecording: CanvasVideoEnhancerRecording ->
                if (cveRecording.isValid()) {
                    if (RecordingStore.add(cveRecording.toRecording()))
                        newRecordings++
                }
            }
            // Update the listener
            listener.update(newRecordings)
            System.out.println("Conversion complete!")
        }
    }

    override fun onFailure(call: Call<List<CanvasVideoEnhancerRecording>>, t: Throwable) {
        t.printStackTrace()
    }
}

class EmptyCallback<T> : Callback<T> {
    override fun onFailure(call: Call<T>?, t: Throwable?) {
        // do nothing
        Log.e("UploadFail", "Upload of recording to server failed")
    }

    override fun onResponse(call: Call<T>?, response: Response<T>?) {
        // do nothing
    }

}