package nz.zhang.lecturerecordingplayer

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun showCanvas(view: View) {
        val canvasIntent = Intent(this, CanvasBrowser::class.java)
        startActivity(canvasIntent)
    }
}
