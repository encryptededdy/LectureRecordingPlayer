package nz.zhang.lecturerecordingplayer.canvasscraper

import okhttp3.Response

class NetworkException(message: String, val response: Response) : RuntimeException(message)