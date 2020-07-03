package cm.ubuea.judie

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.sql.SQLException
import java.util.*

class NoteModule2 : AppCompatActivity(), OnInitListener {
    var tts: TextToSpeech? = null
    var et_title: EditText? = null
    var et_body: EditText? = null
    var et_priority: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.note2_module)
        init()
    }

    private fun init() {
        et_body = findViewById<View>(R.id.et_body) as EditText
        et_priority = findViewById<View>(R.id.et_pri) as EditText
        et_title = findViewById<View>(R.id.et_title) as EditText
        tts = TextToSpeech(this, this)
    }

    private fun promptSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "")
        try {
            startActivityForResult(intent, REQ_CODE)
        } catch (a: ActivityNotFoundException) {
            Toast.makeText(applicationContext,
                    "",
                    Toast.LENGTH_SHORT).show()
        }
    }

    /*
     * Receiving speech input
     */
    private val REQ_CODE = 100
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_CODE -> {
                if (resultCode == Activity.RESULT_OK && null != data) {
                    val result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    fill(result[0])
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    finish()
                }
            }
        }
    }

    var command = Commands.TITLEn
    var body: String? = null
    var title: String? = null
    var priority: String? = null
    private fun fill(result: String) {
        when (command) {
            Commands.BODY -> {
                body = result
                et_body!!.setText(result)
                command = Commands.PRIORITY
                try {
                    ask_fill()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
            Commands.TITLE -> {
                title = result
                et_title!!.setText(result)
                command = Commands.BODY
                try {
                    ask_fill()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
            Commands.PRIORITY -> {
                try {
                    priority = result.toInt().toString()
                } catch (w: Exception) {
                    speakOut(Commands.PRIORITY)
                    while (tts!!.isSpeaking);
                    promptSpeechInput()
                }
                if (priority != null) {
                    et_priority!!.setText(result)
                    command = Commands.SAVE
                    try {
                        ask_fill()
                    } catch (e: SQLException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun speakOut(text: String) {
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null)
    }

    @Throws(SQLException::class)
    private fun ask_fill() {
        when (command) {
            Commands.BODY -> {
                speakOut(Commands.BODY)
                while (tts!!.isSpeaking);
                promptSpeechInput()
            }
            Commands.TITLE -> {
                speakOut(Commands.TITLE)
                while (tts!!.isSpeaking);
                promptSpeechInput()
            }
            Commands.PRIORITY -> {
                speakOut(Commands.PRIORITY)
                while (tts!!.isSpeaking);
                promptSpeechInput()
            }
            Commands.SAVE -> {
                speakOut(Commands.SAVE)
                val db = DataBaseNotes(this)
                db.createEntry(title, body, priority)
                finish()
            }
            else -> {
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported")
            } else {
                //bl.setEnabled(true);
                speakOut("")
                try {
                    ask_fill()
                } catch (e: SQLException) {
                    e.printStackTrace()
                }
            }
        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }

    public override fun onDestroy() {
        // Shuts Down TTS
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }
}