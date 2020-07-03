package cm.ubuea.judie

import android.app.Activity
import android.content.ContentUris
import android.media.MediaPlayer
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import java.io.IOException
import java.util.*
import java.util.logging.Handler

class Music : Activity() {
    private var songList: ArrayList<Song>? = null
    var mediaPlayer: MediaPlayer? = null
    var bstop: ImageButton? = null
    var mtitle: TextView? = null
    var artist: TextView? = null
    var cDuration: TextView? = null
    var tDuration: TextView? = null
    var current: String? = null
    var duration: String? = null
    var seekBar: SeekBar? = null
    var seekHandler: Handler? = null
    var pos = 0
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music)
        val random = Random()
        pos = random.nextInt(20)
        bstop = findViewById<View>(R.id.imageButton) as ImageButton
        mtitle = findViewById<View>(R.id.textViewTitle) as TextView
        artist = findViewById<View>(R.id.textViewArtist) as TextView
        tDuration = findViewById<View>(R.id.textViewTduration) as TextView
        cDuration = findViewById<View>(R.id.textView7) as TextView
        seekBar = findViewById<View>(R.id.seekBar2) as SeekBar
        mediaPlayer = MediaPlayer()
        songList = ArrayList()
        getSongList()
        playSong()
        bstop!!.setOnClickListener {
            mediaPlayer!!.stop()
            mediaPlayer!!.reset()
            task!!.cancel(true)
            finish()
        }
    }

    var task: Task? = null
    private fun playSong() {
        try {
            mediaPlayer!!.reset()

            //get song
            val playSong = songList!![pos]
            mtitle!!.text = playSong.title
            artist!!.text = playSong.artist

            //get id
            val currSong = playSong.id
            //set uri
            val trackUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    currSong)
            try {
                mediaPlayer!!.setDataSource(applicationContext, trackUri)
            } catch (e: Exception) {
                Log.e("MUSIC SERVICE", "Error setting data source", e)
            }
            task = Task()
            mediaPlayer!!.prepare()
            task!!.execute()
            mediaPlayer!!.start()
            seekBar!!.max = mediaPlayer!!.duration
            duration = milliSecondsToTimer(mediaPlayer!!.duration.toLong())
            tDuration!!.text = duration
            cDuration!!.text = duration
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (io: IOException) {
            io.printStackTrace()
        }
    }

    fun getSongList() {
        //retrieve song info
        val musicResolver = contentResolver
        val musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val musicCursor = musicResolver.query(musicUri, null, null, null, null)
        if (musicCursor != null && musicCursor.moveToPosition(3)) {
            //get columns
            val titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            //add songs to list
            do {
                val thisId = musicCursor.getLong(idColumn)
                val thisTitle = musicCursor.getString(titleColumn)
                val thisArtist = musicCursor.getString(artistColumn)
                songList!!.add(Song(thisId, thisTitle, thisArtist))
            } while (musicCursor.moveToNext() && musicCursor.position != 23)
        }
    }

    /**
     * Function to convert milliseconds time to
     * Timer Format
     * Hours:Minutes:Seconds
     */
    fun milliSecondsToTimer(milliseconds: Long): String {
        var finalTimerString = ""
        var secondsString = ""

        // Convert total duration into time
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
        val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()
        // Add hours if there
        if (hours > 0) {
            finalTimerString = "$hours:"
        }

        // Prepending 0 to seconds if it is one digit
        secondsString = if (seconds < 10) {
            "0$seconds"
        } else {
            "" + seconds
        }
        finalTimerString = "$finalTimerString$minutes:$secondsString"

        // return timer string
        return finalTimerString
    }

    fun seekUpdation() {
        seekBar!!.progress = mediaPlayer!!.currentPosition
    }

    override fun onBackPressed() {
        mediaPlayer!!.stop()
        finish()
        super.onBackPressed()
    }

    inner class Task : AsyncTask<Int?, Int?, Void?>() {
        var i: Long = 0
        protected override fun doInBackground(vararg params: Int?): Void? {
            while (mediaPlayer!!.isPlaying) {
                i = mediaPlayer!!.currentPosition.toLong()
                seekUpdation()
                publishProgress(0)
                i++
            }
            return null
        }

        protected override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            tDuration!!.text = "" + milliSecondsToTimer(i)
        }
    }
}