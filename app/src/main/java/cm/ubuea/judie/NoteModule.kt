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
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class NoteModule : AppCompatActivity(), OnInitListener {
    var tts: TextToSpeech? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.note_module)
        val l = findViewById<View>(R.id.lv_note) as ListView
        val l1: BaseAdapter = MyAdapter(this)
        l.adapter = l1
        tts = TextToSpeech(this, this)
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
                promptSpeechInput()
            }
        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }

    private val REQ_CODE = 100
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_CODE -> {
                if (resultCode == Activity.RESULT_OK && null != data) {
                    val result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    command = result[0]
                    execute()
                }
            }
        }
    }

    var command: String? = Commands.MAKE
    private fun execute() {
        when (command) {
            Commands.READ -> {
                var result = ""
                var i = 0
                while (i < MyAdapter.list!!.size) {
                    val sr = MyAdapter.list!![i]!!
                    result = """
                        $result${sr.title}
                        ${sr.body}
                        ${sr.priority}




                        """.trimIndent()
                    i++
                }
                speakOut(result)
            }
            Commands.MAKE -> {
                val i = Intent(this, NoteModule2::class.java)
                startActivity(i)
                finish()
            }
            else -> if (command != null) {
                speakOut("sorry no such Command as $command.....try again")
                while (tts!!.isSpeaking);
                promptSpeechInput()
                command = null
            }
        }
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

    private fun speakOut(text: String) {
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null)
    }
}