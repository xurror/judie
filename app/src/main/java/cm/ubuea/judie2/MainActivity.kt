package cm.ubuea.judie2

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.bhargavms.dotloader.DotLoader
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.dialogflow.v2.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    data class ViewState(
        val spokenText: String,
        val isListening: Boolean,
        val error: String?
    )

    private val ALARM_URI = Uri.parse("android-app://com.myclockapp/set_alarm_page")
    private val TAG = "MainActivity"

    private val generalHelp = """
         - say MAIL to go to mail module\n
         - say PHONE to go to phone module\n
         - say LOCATION to know your current location\n
         - say REMINDER to set a reminder\n
         - say PLAY MUSIC to hear a song\n
         - say NOTE to make a note\n
         - say EMERGENCY in case of emergency\n
         - say DATE or TIME to know current date and time\n
         - say HOW IS THE WEATHER to know current weather\n
     """.trimIndent()
    private val gmailHelp = """
        - To read mails say GET MAILS\n
        - To send a mail say COMPOSE MAIL\n
        - To search mail say SEARCH SUBJECT your subject\n
        - To open a specific category say GET MAILS category\n
    """.trimIndent()
    private val callHelp = """
        - To hear call logs say CALL LOGS\n
        - To hear messages say MESSAGES\n
        - To make a call say CALL NUMBER or CONTACT NAME\n
        - To view a contact say SEARCH CONTACT CONTACT NAME\n
        - To send a message say SEND MESSAGE\n
     """.trimIndent()

    private lateinit var judie: TextToSpeech
    private lateinit var mic: FloatingActionButton
    private lateinit var dotLoader: DotLoader
    private lateinit var input_msg: TextInputEditText

    private var viewState: MutableLiveData<ViewState>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        mic = findViewById<FloatingActionButton>(R.id.mic)
        input_msg = findViewById(R.id.input_msg)
        dotLoader = findViewById(R.id.text_dot_loader)

        initAsisstantVoice()
        checkPermission()
        getVoiceMessage()

//        val preferences = PreferenceManager.getDefaultSharedPreferences(baseContext)
//        welcome = "Hi " + preferences.getString(Needs.NAME, " ") + " what can i do for u today ? "

//        findViewById<FloatingActionButton>(R.id.mic).setOnClickListener {
//          view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                  .setAction("Action", null).show()
//            promptSpeechInput()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun getVoiceMessage() {

        val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, application.packageName)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(bundle: Bundle) {}

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(v: Float) {}

            override fun onBufferReceived(bytes: ByteArray) {}

            override fun onEndOfSpeech() {}

            override fun onError(i: Int) {}

            override fun onResults(bundle: Bundle) {
                //getting all the matches
                val matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null) {
                    var msg = matches.get(0)
                    input_msg.setText(matches.get(0))
                    appendText(msg)
                    Log.i(TAG, matches.get(0))
                }
            }

            override fun onPartialResults(bundle: Bundle) {}

            override fun onEvent(i: Int, bundle: Bundle) {}
        })

        mic.setOnTouchListener(View.OnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_UP -> {
                    speechRecognizer.stopListening()
                    dotLoader.visibility = View.INVISIBLE
                }

                MotionEvent.ACTION_DOWN -> {
                    vibrate()
                    speechRecognizer.startListening(recognizerIntent)
                    dotLoader.visibility = View.VISIBLE
                }
            }
            false
        })
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
                startActivity(intent)
                finish()
                Toast.makeText(this, "Enable Microphone Permission..!!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun vibrate() {
        val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200)
        }
    }

//    New Stuffs

    private fun initAsisstantVoice() {
        judie = TextToSpeech(applicationContext, object : TextToSpeech.OnInitListener {
            override fun onInit(status: Int) {
                if (status == TextToSpeech.SUCCESS) {
                    val result = judie.setLanguage(Locale.getDefault())
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported")
                    }
                } else {
                    Log.e("TTS", "Initialization Failed!")
                }
            }
        })
    }

//    private fun initAsisstant() {
//        try {
//            val stream = resources.openRawResource(R.raw.asistan)
//            val credentials = GoogleCredentials.fromStream(stream)
//            val projectId = (credentials as ServiceAccountCredentials).projectId
//
//            val settingsBuilder = SessionsSettings.newBuilder()
//            val sessionsSettings = settingsBuilder.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build()
//            client = SessionsClient.create(sessionsSettings)
//            session = SessionName.of(projectId, uuid)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//    }


    private fun sendMessage() {
        val msg = input_msg.text.toString()
        if (msg.trim { it <= ' ' }.isEmpty()) {
            Toast.makeText(this@MainActivity, "Message sent", Toast.LENGTH_LONG).show()
        } else {
//            appendText(msg, USER)
            appendText(msg)
            input_msg.setText("")

//            // Java V2
//            val queryInput = QueryInput.newBuilder().setText(TextInput.newBuilder().setText(msg).setLanguageCode("tr")).build()
//            RequestTask(this@MainActivity, session!!, client!!, queryInput).execute()
        }
    }

//    private fun appendText(message: String, type: Int) {
    private fun appendText(message: String) {
        val layout: FrameLayout = appendUserText()
//        when (type) {
//            USER -> layout = appendUserText()
//            BOT -> layout = appendBotText()
//            else -> layout = appendBotText()
//        }
        layout.isFocusableInTouchMode = true
        linear_chat.addView(layout)
        val tv = layout.findViewById<MaterialTextView>(R.id.user_logo)
        tv.setText(message)
//        Util.hideKeyboard(this)
        layout.requestFocus()
        input_msg.requestFocus() // change focus back to edit text to continue typing

//        @Suppress("DEPRECATION")
//        if(type != USER) {
//            judie.speak(message,TextToSpeech. QUEUE_FLUSH,null)
//        }
    }


    fun appendUserText(): FrameLayout {
        val inflater = LayoutInflater.from(this@MainActivity)
        return inflater.inflate(R.layout.user_message_layout, null) as FrameLayout
    }

    fun appendBotText(): FrameLayout {
        val inflater = LayoutInflater.from(this@MainActivity)
        return inflater.inflate(R.layout.judie_message_layout, null) as FrameLayout
    }

//    fun onResult(response: DetectIntentResponse?) {
//        try {
//            if (response != null) {
//                var botReply:String=""
//                if(response.queryResult.fulfillmentText==" ")
//                    botReply= response.queryResult.fulfillmentMessagesList[0].text.textList[0].toString()
//                else
//                    botReply= response.queryResult.fulfillmentText
//
//                appendText(botReply, BOT)
//            } else {
//                appendText(getString(R.string.anlasilmadi), BOT)
//            }
//        }catch (e:Exception){
//            appendText(getString(R.string.anlasilmadi), BOT)
//        }
//    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if(judie != null){
            judie?.stop()
            judie?.shutdown()
        }
    }

//    End of New Stuffs

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            // User chose the "Settings" item, show the app settings UI...
            Toast.makeText(this, "Settings...!!", Toast.LENGTH_SHORT).show()
            true
        }

        R.id.action_help -> {
            // User chose the "Favorite" action, mark the current item
            // as a favorite...
            Toast.makeText(this, "Help...!!", Toast.LENGTH_SHORT).show()
            MaterialAlertDialogBuilder(this).apply {
                setTitle(resources.getString(R.string.title_help))
                setMessage(generalHelp)
                setNeutralButton(resources.getString(R.string.cancel)) { dialog, which ->
                        // Respond to neutral button press
                }
            }.show()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }
}

