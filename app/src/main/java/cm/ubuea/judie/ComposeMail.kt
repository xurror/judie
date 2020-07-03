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
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class ComposeMail : AppCompatActivity(), OnInitListener, Runnable {
    var tts: TextToSpeech? = null
    var et_to: EditText? = null
    var et_subject: EditText? = null
    var et_body: EditText? = null
    var result = Commands.MAIL_TO
    var bundle: Bundle? = null
    var t: Thread? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.compose_mails)
        init()
    }

    private fun init() {
        et_body = findViewById<View>(R.id.et_body) as EditText
        et_subject = findViewById<View>(R.id.et_subject) as EditText
        et_to = findViewById<View>(R.id.et_to) as EditText
        tts = TextToSpeech(this, this)
        t = Thread(this)
        bundle = Bundle()
        val b = findViewById<View>(R.id.b_send) as Button
        b.setOnClickListener {
            try {
                Thread.sleep(2000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            speakOut(Commands.MAIL_SEND)
            val i = Intent()
            i.putExtras(bundle!!)
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            setResult(Activity.RESULT_OK, i)
            finish()
        }
    }

    private fun promptSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt))
        try {
            startActivityForResult(intent, REQ_CODE)
        } catch (a: ActivityNotFoundException) {
            Toast.makeText(applicationContext,
                    getString(R.string.speech_not_supported),
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
                    if (this.result !== Commands.MAIL_CONFIRM) fill(result[0]) else confirm(result[0])
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    finish()
                }
            }
        }
    }

    var fill = false
    private fun fill(result: String) {
        var result = result
        when (this.result) {
            Commands.MAIL_TO -> {
                result = Util.mail(result)
                et_to!!.setText(result)
                bundle!!.putString(Commands.MAIL_TO, result +
                        "")
                if (!fill) this.result = Commands.MAIL_SUBJECT else this.result = Commands.MAIL_CONFIRM
                ask_fill()
            }
            Commands.MAIL_SUBJECT -> {
                et_subject!!.setText(result)
                bundle!!.putString(Commands.MAIL_SUBJECT, result)
                if (!fill) this.result = Commands.MAIL_BODY else this.result = Commands.MAIL_CONFIRM
                ask_fill()
            }
            Commands.MAIL_BODY -> {
                et_body!!.setText(result)
                bundle!!.putString(Commands.MAIL_BODY, result)
                this.result = Commands.MAIL_CONFIRM
                fill = true
                ask_fill()
            }
        }
    }

    private fun confirm(result: String) {
        when (result) {
            Commands.MAIL_TO -> {
                this.result = Commands.MAIL_TO
                ask_fill()
            }
            Commands.MAIL_SUBJECT -> {
                this.result = Commands.MAIL_SUBJECT
                ask_fill()
            }
            Commands.MAIL_BODY -> {
                this.result = Commands.MAIL_BODY
                ask_fill()
            }
            Commands.MAIL_SEND -> {
                this.result = Commands.MAIL_SEND
                ask_fill()
            }
            else -> {
                this.result = Commands.MAIL_CONFIRM
                ask_fill()
            }
        }
    }

    private fun speakOut(text: String) {
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null)
    }

    private fun ask_fill() {
        when (result) {
            Commands.MAIL_TO -> {
                speakOut(Commands.MAIL_TO)
                while (tts!!.isSpeaking);
                promptSpeechInput()
            }
            Commands.MAIL_SUBJECT -> {
                speakOut(Commands.MAIL_SUBJECT)
                while (tts!!.isSpeaking);
                promptSpeechInput()
            }
            Commands.MAIL_BODY -> {
                speakOut(Commands.MAIL_BODY)
                while (tts!!.isSpeaking);
                promptSpeechInput()
            }
            Commands.MAIL_SEND -> {
                try {
                    Thread.sleep(2000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                speakOut(Commands.MAIL_SEND)
                val i = Intent()
                i.putExtras(bundle!!)
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                setResult(Activity.RESULT_OK, i)
                finish()
            }
            Commands.MAIL_CONFIRM -> {
                speakOut(Commands.MAIL_CONFIRM)
                while (tts!!.isSpeaking);
                promptSpeechInput()
            }
        }
    }

    var a = false
    override fun onPostResume() {
        super.onPostResume()
        a = true
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
                while (!a);
                ask_fill()
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

    override fun run() {
        // fill();
    }
}