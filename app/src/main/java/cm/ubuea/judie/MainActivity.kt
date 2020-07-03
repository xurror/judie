package cm.ubuea.judie

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import cm.ubuea.judie.GmailModule
import cm.ubuea.judie.MainActivity
import org.xml.sax.InputSource
import java.net.URL
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.parsers.SAXParserFactory

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
@TargetApi(Build.VERSION_CODES.DONUT)
class MainActivity : Activity(), OnInitListener {
    private var showUspeak: TextView? = null
    private var dateView: TextView? = null
    private var help: Button? = null
    private var speak: ImageButton? = null
    private var command = "blabla"
    private var check = false
    private val REQ_CODE = 100
    private var tts: TextToSpeech? = null
    private var welcome: String? = null
    private var date: String? = null
    private var city = "Buea"
    private var country = "Cameroon"
    private val baseUrl = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22" +
            city + "%2C%20" + country + "%22)&format=xml&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys"
    var weatherText: String? = null

    @TargetApi(Build.VERSION_CODES.DONUT)
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val preferences = PreferenceManager.getDefaultSharedPreferences(baseContext)
        welcome = "Hi " + preferences.getString(Needs.NAME, " ") + " what can i do for u today ? "
        //Grabbing References
        showUspeak = findViewById<View>(R.id.textViewShow) as TextView
        help = findViewById<View>(R.id.buttonHelp) as Button
        speak = findViewById<View>(R.id.imageButtonSpeak) as ImageButton
        tts = TextToSpeech(this, this)
        val df: DateFormat = SimpleDateFormat("EEE, d MMM yyyy, h:mm a")
        date = df.format(Calendar.getInstance().time)
        dateView = findViewById<View>(R.id.textView7Date) as TextView
        dateView!!.text = date
        MyTask().execute()
        //Welcome
        showUspeak!!.text = welcome
        tts!!.speak(welcome, TextToSpeech.QUEUE_FLUSH, null)
        speak!!.setOnClickListener { //Prompt speech input
            promptSpeechInput()
            check = true
        }
        help!!.setOnClickListener { launchModule(Commands.helpModule) }
    }

    private fun launchModule(commandTolaunch: String) {
        when (commandTolaunch) {
            Commands.mailModule -> {
                Toast.makeText(baseContext, "Mail Module", Toast.LENGTH_SHORT).show()
                val intentm = Intent(this@MainActivity, GmailModule::class.java)
                startActivity(intentm)
            }
            Commands.callModule -> {
                Toast.makeText(baseContext, "Call Module", Toast.LENGTH_SHORT).show()
                val intentc = Intent(this@MainActivity, PhoneModule::class.java)
                startActivity(intentc)
            }
            Commands.emergencyModule -> {
                Toast.makeText(baseContext, "Emergency Module", Toast.LENGTH_SHORT).show()
                val intente = Intent(this@MainActivity, MapModule::class.java)
                intente.putExtra(Commands.EMERGENCY, true)
                startActivity(intente)
            }
            Commands.locModule -> {
                Toast.makeText(baseContext, "Location Module", Toast.LENGTH_SHORT).show()
                val intentl = Intent(this@MainActivity, MapModule::class.java)
                startActivity(intentl)
            }
            Commands.musicModule -> {
                Toast.makeText(baseContext, "Music Module", Toast.LENGTH_SHORT).show()
                val intentmu = Intent(this@MainActivity, Music::class.java)
                startActivity(intentmu)
            }
            Commands.DATE -> {
                val d = DisplayFrag()
                val bundle = Bundle()
                bundle.putString(Commands.DATE, date)
                d.arguments = bundle
                d.show(d.fragmentManager!!, "sss")
            }
            Commands.TIME -> {
                val d2 = DisplayFrag()
                val bundle2 = Bundle()
                bundle2.putString(Commands.DATE, date)
                d2.arguments = bundle2
                d2.show(d2.fragmentManager!!, "sss")
            }
            Commands.remmodule -> {
                Toast.makeText(baseContext, "Reminder Module", Toast.LENGTH_SHORT).show()
                val intentr = Intent(this@MainActivity, ReminderModule::class.java)
                startActivity(intentr)
            }
            Commands.helpModule -> {
                module = "main"
                Toast.makeText(baseContext, "Help Module", Toast.LENGTH_SHORT).show()
                val frag = HelpFrag()
                frag.show(frag.fragmentManager!!, null)
            }
            Commands.noteModule -> {
                Toast.makeText(baseContext, "Note Module", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@MainActivity, NoteModule::class.java)
                startActivity(intent)
            }
            Commands.weather -> {
                val d1 = DisplayFrag()
                val bundle1 = Bundle()
                bundle1.putString(Commands.DATE, weatherText)
                d1.arguments = bundle1
                d1.show(d1.fragmentManager!!, "sss")
            }
            else -> try {
                val intents = Intent(Intent.ACTION_WEB_SEARCH)
                intents.putExtra(SearchManager.QUERY, commandTolaunch)
                startActivity(intents)
            } catch (e: Exception) {
                Log.println(Log.ERROR, "An Exception occurred", e.message)
            }
        }
    }

    /**
     * Showing google speech input dialog
     */
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

    @SuppressLint("StaticFieldLeak")
    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    internal inner class MyTask : AsyncTask<Void?, Void?, Void?>() {
        var doing: XMLWorker? = null
        protected override fun doInBackground(vararg params: Void?): Void? {
            try {
                val web = URL(baseUrl)
                val saxParserFactory = SAXParserFactory.newInstance()
                val sp = saxParserFactory.newSAXParser()
                val xmlReader = sp.xmlReader
                doing = XMLWorker()
                xmlReader.contentHandler = doing
                xmlReader.parse(InputSource(web.openStream()))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }

        var command = Commands.TEMP
        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            when (command) {
                Commands.TEMP -> weatherText = doing!!.getTemp()
            }
        }
    }

    /**
     * Receiving speech input
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_CODE -> {
                if (resultCode == Activity.RESULT_OK && null != data) {
                    val result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    showUspeak!!.text = result[0]

                    //Speak out
                    speakOut()
                }
            }
        }
    }

    //Speak Out
    @TargetApi(Build.VERSION_CODES.DONUT)
    private fun speakOut() {
        val text = showUspeak!!.text.toString()
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        command = text

        //Launch Module
        if (check) {
            launchModule(command)
        }
    }

    @TargetApi(Build.VERSION_CODES.DONUT)
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported")
            } else {
                speak!!.isEnabled = true
                speakOut()
            }
        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }

    @TargetApi(Build.VERSION_CODES.DONUT)
    public override fun onDestroy() {
        // Shuts Down TTS
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

    companion object {
        @JvmField
        var module = ""
    }
}