package nz.zhang.lecturerecordingplayer.canvasscraper

import android.util.Log
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import nz.zhang.lecturerecordingplayer.recordings.Recording
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BASIC
import java.util.regex.Pattern

class CanvasScraper constructor(private val authCookie: String, private val listener: ScraperListener) {

    private val okHttpClient: OkHttpClient

    private val jsonParser = JsonParser()

    private val apiUrl = HttpUrl.Builder().scheme("https").host("canvas.auckland.ac.nz").addPathSegments("api/v1").build()

    private val recordingRegex = "https://mediastore\\.auckland\\.ac\\.nz/.{0,100}(\\.preview|\\.mp4|\\.m4v|\\.mp3|-slides\\.m4v)"

    private val visitedUrls: MutableSet<HttpUrl> = HashSet()

    //private val lectureUrls: MutableSet<HttpUrl> = HashSet()

    init {
        // Log the requests
        val logging = HttpLoggingInterceptor().apply { level = BASIC }
        okHttpClient = OkHttpClient.Builder().addInterceptor(logging).build()
    }

    fun run(courseIds:List<Int>) {
        // val courseIds = courseIds()
        //val courseIds = arrayOf(23575, 23572)
        courseIds.forEach(this::parseForLectureUrls)
        // Display URLs
        //println(Gson().toJson(lectureUrls))
        listener.complete()
    }

    private fun foundLectureURL(url: HttpUrl) {
        val recording = Recording(url.toString())
        if (recording.isValid) {
            listener.update(recording)
        }
    }

    private fun parseForLectureUrls(courseId: Int) {
        val pagesToVisit = arrayOf("modules", "pages")
        pagesToVisit.forEach { page ->
            val apiUrl = this.apiUrl.newBuilder()
                    .addPathSegments("courses/$courseId/$page")
                    .addQueryParameter("per_page", "100")
                    .build()
            parseForLectureUrls(apiUrl)
        }
    }

    private fun parseForLectureUrls(apiUrl: HttpUrl) {
        // Don't visit pages more than once
        if (apiUrl in visitedUrls) {
            return
        }
        visitedUrls += apiUrl
        // Make the API call
        try {
            val json = apiCall(apiUrl)
            // Parse the page for lecture links
            parseForLectureUrls(json)
        } catch (e: NetworkException) {
            // Allow bad responses where the content could not be found
            // And allow cases where the response code is OK i.e. the content
            // type of the response was not what we expected
            if (e.response.code() == 404 || e.response.code() in 200..299) {
                // Didn't find anything, so it's k
            } else {
                throw e
            }
        }
    }

    private fun parseForLectureUrls(json: JsonElement) {
        if (json.isJsonObject) {
            val jsonObject = json.asJsonObject
            // Loop through the objects & arrays to recursively visit the pages
            jsonObject.entrySet().forEach { pair ->
                val value = pair.value
                // Recursively parse if inner objects & arrays
                if (value.isJsonArray || value.isJsonObject) {
                    parseForLectureUrls(value)
                }
            }
            // This is used for Canvas pages, body contains the HTML of the page
            if (jsonObject["body"] != null) {
                // Match all of the lecture recording links & add them to the list
                val matcher = Pattern.compile(recordingRegex)
                        .matcher("${json.asJsonObject["body"]}")
                while (matcher.find()) {
                    val match = matcher.group()
                    // Parse it to make sure it's a valid URL
                    // Todo - should we just switch these all to normal strings? I mean the REGEX handles the valid URL part
                    // Todo - but other links may still require this conversion to check? Also, does Canvas ever return relative URLs?
                    // Todo - if they return relative URLs as url, html_url, external_url then we need to resolve them first
                    val parse = HttpUrl.parse(match)
                    if (parse != null) {
                        foundLectureURL(parse)
                    } else {
                        continue
                    }
                }
            }
            // Check these properties in order i.e. give them priority
            val toCheck = arrayOf("url", "external_url", "items_url", "html_url")
            for (propertyName in toCheck) {
                val propertyValue = jsonObject[propertyName]
                if (propertyValue != null && !propertyValue.isJsonNull) {
                    if (propertyValue.asString.endsWith("/modules") || propertyValue.asString.endsWith("/items")) {
                        visitCanvasJsonUrl(propertyValue.asString+"?per_page=100") // if we're in modules or items, read more
                    }
                    visitCanvasJsonUrl(propertyValue.asString) // read more pages
                    break
                }
            }
        } else if (json.isJsonArray) {
            json.asJsonArray.forEach { element ->
                // Recursively parse object
                parseForLectureUrls(element)
            }
        }
    }

    // Check or visit some strange URL that Canvas returns in some API call
    private fun visitCanvasJsonUrl(value: String) {
        // Try parsing the value as a HttpUrl and ignore links we've already parsed
        val url = HttpUrl.parse(value) ?: return
        // Todo - maybe refactor this matching
        when {
            recordingRegex.toRegex().matches("$url") -> {
                foundLectureURL(url)
            }
            url.host().contains("canvas.auckland.ac.nz") -> {
                val apiUrl = url.let {
                    val path = it.pathSegments()
                    // Prepend the path with /api/v1/
                    // The HttpUrl API doesn't allow for that easily
                    if (path.size > 0 && path[0] != "api") {
                        val apiUrl = "$url".replace(
                                "${url.host()}/", "${url.host()}/api/v1/")
                        return@let HttpUrl.parse(apiUrl) ?: it
                    } else {
                        return@let it
                    }
                }
                parseForLectureUrls(apiUrl)
            }
            else -> println("Unknown URL: $url")
        }
    }

    fun getCourseData(listener: ScraperCourseListListener) {
        // https://canvas.auckland.ac.nz/api/v1/courses
        val coursesUrl = apiUrl.newBuilder().addPathSegment("courses").addQueryParameter("per_page", "100").build()
        val json = apiCall(coursesUrl)
        System.out.println("Found JSON: "+json.toString())
        val ids = ArrayList<Int>()
        val names = ArrayList<String>()
        for (obj:JsonElement in json.asJsonArray) {
            if (obj.asJsonObject["course_code"] != null) {
                ids.add(obj.asJsonObject["id"].asInt)
                names.add(obj.asJsonObject["course_code"].asString)
            }
        }
        listener.update(ids, names, this)
    }

    private fun apiCall(apiUrl: HttpUrl): JsonElement {
        // Execute the request
        val request = Request.Builder().authenticate().url(apiUrl).build()
        val response = okHttpClient.newCall(request).execute()
        val validate = response.validate(contentType = "json")
        if (validate == null) {
            Log.e("CanvasScraper", "Malformed response")
            return JsonObject() // empty
        }
        // Remove "while(1);" prefix that Canvas returns for whatever reason
        val removePrefix = response?.body()?.string()?.removePrefix("while(1);")
        if (removePrefix != null) return jsonParser.parse(removePrefix) else return JsonObject()
    }

    private fun Request.Builder.authenticate(): Request.Builder {
        return this.header("Cookie", authCookie)
    }

    private fun Response.validate(contentType: String? = null, autoClose: Boolean = true): Response? {
        if (code() !in 200..299) {
            if (autoClose) {
                this@validate.body()?.close()
            }
            return null
        }
        if (contentType != null && this@validate.header("Content-Type", null)?.contains(contentType) != true) {
            if (autoClose) {
                this@validate.body()?.close()
            }
            return null
        }
        return this@validate
    }

}