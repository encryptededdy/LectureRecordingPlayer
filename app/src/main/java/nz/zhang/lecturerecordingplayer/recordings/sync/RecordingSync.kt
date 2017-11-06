package nz.zhang.lecturerecordingplayer.recordings.sync

import nz.zhang.lecturerecordingplayer.recordings.Recording
import nz.zhang.lecturerecordingplayer.recordings.RecordingStore
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Response


const val API_ROOT_URL = "https://canvasvideoenhancer.azurewebsites.net/"

class RecordingSync : Callback<List<CanvasVideoEnhancerRecording>> {
    var downloadedRecordings = ArrayList<CanvasVideoEnhancerRecording>()
    var callsMade = 0
    val courses:List<Course>

    init {
        courses = populateCourses()
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
            System.out.println("Recieved all callbacks!")
            // All playlists downloaded! Now convert to recording and add them
            downloadedRecordings.forEach { cveRecording: CanvasVideoEnhancerRecording ->
                if (cveRecording.isValid()) {
                    RecordingStore.add(cveRecording.toRecording())
                }
            }
            System.out.println("Conversion complete!")
        }
    }

    override fun onFailure(call: Call<List<CanvasVideoEnhancerRecording>>, t: Throwable) {
        t.printStackTrace()
    }

    // Gets courses
    private fun populateCourses() : List<Course> {
        val uniqueCourses = HashSet<Course>()
        RecordingStore.recordings.forEach { recording: Recording ->
            uniqueCourses.add(Course(recording.courseName + recording.courseNumber + recording.courseStream, recording.semesterNumber))}
        return uniqueCourses.toList()
    }
}

data class Course(val course: String, val semesterCode: String)