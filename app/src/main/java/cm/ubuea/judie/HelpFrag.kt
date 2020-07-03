package cm.ubuea.judie

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import java.util.*

class HelpFrag : DialogFragment(), OnInitListener {
    var text = ""
    var i: IsSpeaking? = null
    var module = MainActivity.module
    private var tts: TextToSpeech? = null
    var main = """say MAIL to go to mail module
     say PHONE to go to phone module
     say LOCATION to know your current location
     say REMINDER to set a reminder
     say PLAY MUSIC to hear a song
     say NOTE to make a note
     say EMERGENCY in case of emergency
     say DATE or TIME to know current date and time
     say HOW IS THE WEATHER to know current weather"""
        var gmail = """To read mails say GET MAILS
     To send a mail say COMPOSE MAIL
     To search mail say SEARCH SUBJECT your subject
     To open a specific category say GET MAILS category"""
        var call = """To hear call logs say CALL LOGS
     To hear messages say MESSAGES
     To make a call say CALL NUMBER or CONTACT NAME
     To view a contact say SEARCH CONTACT CONTACT NAME
     To send a message say SEND MESSAGE"""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_help, container, false)
        tvd = view.findViewById<View>(R.id.tv_displayh) as TextView
        tts = TextToSpeech(activity!!.baseContext, this)
        i = IsSpeaking(tts!!, this)
        appropriateHelp(module)
        return view
    }

    private fun appropriateHelp(whichModule: String) {
        when (whichModule) {
            "main" -> {
                text = main
                tvd!!.text = text
                speakOut()
            }
            "gmail" -> {
                text = gmail
                tvd!!.text = text
                speakOut()
            }
            "phone" -> {
                text = call
                tvd!!.text = text
                speakOut()
            }
            else -> {
                text = "Invalid command"
                speakOut()
            }
        }
    }

    //Speak Out
    private fun speakOut() {
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported")
            } else {
                speakOut()
                i!!.start()
            }
        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }

    override fun onDestroy() {
        // Shuts Down TTS
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        super.onDestroy()
    }

    companion object {
        var tvd: TextView? = null
    }
}