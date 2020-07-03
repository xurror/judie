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

class PhoneFragment : DialogFragment(), OnInitListener, Runnable {
    private var textToSpeech: TextToSpeech? = null
    var i: Thread? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.diplay_msgs, container)
        val msg = view.findViewById<View>(R.id.tv_display) as TextView
        msg.text = PhoneModule.result
        textToSpeech = TextToSpeech(activity!!.baseContext, this)
        i = Thread(this)
        return view
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech!!.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported")
            } else {
                i!!.start()


                //while (textToSpeech.isSpeaking());
                //dismiss();
            }
        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }

    private fun speakOut(text: String) {
        textToSpeech!!.speak(text, TextToSpeech.QUEUE_ADD, null)
        while (!textToSpeech!!.isSpeaking);
    }

    override fun run() {
        speakOut(PhoneModule.result!!)
        while (textToSpeech!!.isSpeaking);
        dismiss()
    }

    override fun onDestroy() {
        // Shuts Down TTS
        if (textToSpeech != null) {
            textToSpeech!!.stop()
            textToSpeech!!.shutdown()
        }
        super.onDestroy()
    }
}