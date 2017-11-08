package nz.zhang.lecturerecordingplayer.canvasscraper

import nz.zhang.lecturerecordingplayer.recordings.Recording

interface ScraperListener {
    fun update(recording: Recording)
    fun complete()
}

interface ScraperCourseListListener {
    fun update(ids: List<Int>, names:List<String>, scraper:CanvasScraper)
}