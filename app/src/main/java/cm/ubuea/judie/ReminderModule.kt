package cm.ubuea.judie

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import java.sql.SQLException
import java.util.*

class ReminderModule : Activity(), OnInitListener {
    var tv: TextView? = null
    var et_date: EditText? = null
    var et_month: EditText? = null
    var et_year: EditText? = null
    var et_hour: EditText? = null
    var et_minutes: EditText? = null
    var et_title: EditText? = null
    var time: Long = 3000
    var textToSpeech: TextToSpeech? = null
    var result: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.module_reminder)
        init()
    }

    private fun init() {
        textToSpeech = TextToSpeech(this, this)
        et_date = findViewById<View>(R.id.et_date) as EditText
        et_year = findViewById<View>(R.id.et_year) as EditText
        et_month = findViewById<View>(R.id.et_month) as EditText
        et_hour = findViewById<View>(R.id.et_hour) as EditText
        et_minutes = findViewById<View>(R.id.et_minutes) as EditText
        et_title = findViewById<View>(R.id.et_title) as EditText
    }

    fun scheduleAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val db = DataBase(this)
        try {
            db.Open()
            val r = Random(9999)
            val id = r.nextInt().toString()
            intent.putExtra("id", id)
            db.createEntry(id, title, desc)
            db.close()
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val date = Date()
        val time = date.time
        val date1 = Date(year % 2000 + 100, month - 1, this.date, hour, minutes, 0)
        val time2 = date1.time
        this.time = time2 - time
        alarmManager[AlarmManager.RTC_WAKEUP, GregorianCalendar().timeInMillis + this.time] = pendingIntent
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)
    }

    private val REQ_CODE = 100
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_CODE -> {
                if (resultCode == RESULT_OK && null != data) {
                    val result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    this.result = result[0]
                    fill()
                }
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

    private fun askFill() {
        when (command) {
            Commands.DATE -> {
                speakout(Commands.DATE)
                promptSpeechInput()
            }
            Commands.YEAR -> {
                speakout(Commands.YEAR)
                promptSpeechInput()
            }
            Commands.MONTH -> {
                speakout(Commands.MONTH)
                promptSpeechInput()
            }
            Commands.HOUR -> {
                speakout(Commands.HOUR)
                promptSpeechInput()
            }
            Commands.MINUTES -> {
                speakout(Commands.MINUTES)
                promptSpeechInput()
            }
            Commands.TITLE -> {
                speakout(Commands.TITLE)
                promptSpeechInput()
            }
            Commands.SET -> scheduleAlarm()
        }
    }

    var date = 0
    var year = 0
    var month = 0
    var hour = 0
    var minutes = 0
    var title: String? = null
    var desc = "Hey u wanted me to remind you !!"
    var command = Commands.YEAR
    private fun fill() {
        when (command) {
            Commands.DATE -> {
                //while (textToSpeech.isSpeaking());
                et_date!!.setText(result)
                date = result!!.toInt()
                command = Commands.HOUR
                askFill()
            }
            Commands.MONTH -> {
                //while (textToSpeech.isSpeaking());
                et_month!!.setText(result)
                month = result!!.toInt()
                command = Commands.DATE
                askFill()
            }
            Commands.YEAR -> {
                //while (textToSpeech.isSpeaking());
                et_year!!.setText(result)
                year = result!!.toInt()
                command = Commands.MONTH
                askFill()
            }
            Commands.HOUR -> {
                //while (textToSpeech.isSpeaking());
                et_hour!!.setText(result)
                hour = result!!.replace("one", "1").toInt()
                command = Commands.MINUTES
                askFill()
            }
            Commands.MINUTES -> {
                //while (textToSpeech.isSpeaking());
                et_minutes!!.setText(result)
                minutes = result!!.toInt()
                command = Commands.TITLE
                askFill()
            }
            Commands.TITLE -> {
                //while (textToSpeech.isSpeaking());
                et_title!!.setText(result)
                title = result
                command = Commands.SET
                askFill()
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            askFill()
        }
    }

    private fun speakout(text: String) {
        textToSpeech!!.speak(text, TextToSpeech.QUEUE_FLUSH, null)
    }

    public override fun onDestroy() {
        // Shuts Down TTS
        if (textToSpeech != null) {
            textToSpeech!!.stop()
            textToSpeech!!.shutdown()
        }
        super.onDestroy()
        finish()
    }
}