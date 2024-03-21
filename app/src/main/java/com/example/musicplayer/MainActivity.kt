package com.example.musicplayer

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var timer: Timer
    private var songDuration: Int = 0
    private lateinit var songListView: ListView
    private val songList = arrayOf(
        "Song1",
        "Song2",
        "Song3",
        "Song4",
        "Song5",
        "Song6"// Replace with your actual song names
    )
    private var currentSongIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize views
        val playButton = findViewById<ImageButton>(R.id.playButton)
        val pauseButton = findViewById<ImageButton>(R.id.pauseButton)
        val stopButton = findViewById<ImageButton>(R.id.stopButton)
        val previousButton = findViewById<ImageButton>(R.id.previousButton)
        val nextButton = findViewById<ImageButton>(R.id.nextButton)
        val playlistButton = findViewById<ImageButton>(R.id.playlistButton)
        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        val songDurationTextView = findViewById<TextView>(R.id.songDuration)
        songListView = findViewById(R.id.songListView)

        // Populate the ListView with song names
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, songList)
        songListView.adapter = adapter

        // Initially hide the ListView
        songListView.visibility = View.INVISIBLE

        // Set the initial song
        initializeSong(songList[currentSongIndex])

        // Set up click listeners and functionality
        playButton.setOnClickListener {
            mediaPlayer.start()
            startTimer()
        }

        pauseButton.setOnClickListener {
            mediaPlayer.pause()
            stopTimer()
        }

        stopButton.setOnClickListener {
            mediaPlayer.stop()
            mediaPlayer.prepare()
            stopTimer()
            seekBar.progress = 0
        }

        previousButton.setOnClickListener {
            // Play the previous song
            currentSongIndex = (currentSongIndex - 1 + songList.size) % songList.size
            initializeSong(songList[currentSongIndex])
            mediaPlayer.start()
            startTimer()
        }

        nextButton.setOnClickListener {
            // Play the next song
            currentSongIndex = (currentSongIndex + 1) % songList.size
            initializeSong(songList[currentSongIndex])
            mediaPlayer.start()
            startTimer()
        }

        playlistButton.setOnClickListener {
            // Toggle the visibility of the ListView
            if (songListView.visibility == View.VISIBLE) {
                songListView.visibility = View.INVISIBLE
            } else {
                songListView.visibility = View.VISIBLE
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // Do nothing when the progress changes, we'll handle this in onStartTrackingTouch
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Pause the media player when the user starts touching the SeekBar
                mediaPlayer.pause()
                stopTimer()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // When the user stops touching the SeekBar, seek the media player to the new position and resume playing
                val newPosition = seekBar?.progress ?: 0
                mediaPlayer.seekTo((newPosition * songDuration) / 100)
                mediaPlayer.start()
                startTimer()
            }
        })

        songListView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            currentSongIndex = position
            initializeSong(songList[currentSongIndex])
            mediaPlayer.start()
            startTimer()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun initializeSong(songName: String) {
        // Release the previous MediaPlayer instance (if any)
        if (this::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }

        // You need to implement a method to retrieve the resource ID based on the song name
        val songResId = getSongResourceId(songName)

        // Create a new MediaPlayer instance and load the selected song
        mediaPlayer = MediaPlayer.create(this, songResId)
        songDuration = mediaPlayer.duration

        // Update the song duration TextView
        val durationInMinutes = songDuration / (1000 * 60)
        val durationInSeconds = (songDuration % (1000 * 60)) / 1000
        findViewById<TextView>(R.id.songDuration).text = String.format("%02d:%02d", durationInMinutes, durationInSeconds)

        // Reset the SeekBar progress
        findViewById<SeekBar>(R.id.seekBar).progress = 0
    }

    // You need to implement this method to retrieve the resource ID based on the song name
    private fun getSongResourceId(songName: String): Int {
        // Logic to map song names to resource IDs
        // You can use a HashMap, switch-case, or any other method to achieve this
        // For demonstration, I'll assume that the song names match the resource names
        return resources.getIdentifier(songName.toLowerCase(), "raw", packageName)
    }


    private fun startTimer() {
        timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    val currentPosition = mediaPlayer.currentPosition
                    val seekBarProgress = (currentPosition * 100) / songDuration
                    findViewById<SeekBar>(R.id.seekBar).progress = seekBarProgress

                    // Update the song duration TextView
                    val remainingTime = songDuration - currentPosition
                    val remainingMinutes = (remainingTime / 1000) / 60
                    val remainingSeconds = (remainingTime / 1000) % 60
                    findViewById<TextView>(R.id.songDuration).text = String.format("%02d:%02d", remainingMinutes, remainingSeconds)
                }
            }
        }, 0, 1000)
    }


    private fun stopTimer() {
        timer.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }
}

