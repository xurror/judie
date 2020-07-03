package cm.ubuea.judie

import android.annotation.SuppressLint
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import java.util.*

class DisplayMsgs : DialogFragment(), OnInitListener {
    private var textToSpeech: TextToSpeech? = null
    private var tvDispay: TextView? = null
    var i: IsSpeaking? = null
    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.diplay_msgs, null)
        tvDispay = view.findViewById<View>(R.id.tv_display) as TextView
        tvDispay!!.text = GmailModule.mOutputText
        textToSpeech = TextToSpeech(activity!!.baseContext, this)
        i = IsSpeaking(textToSpeech!!, this)
        return view
    }

    @SuppressLint("SetTextI18n")
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech!!.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tvDispay!!.text = "TTS  This Language is not supported"
            } else {
                //bl.setEnabled(true);
                speakOut(GmailModule.mOutputText)
                i!!.start()
                // dismiss();
            }
        } else {
            tvDispay!!.text = "TTS Initilization Failed!"
        }
    }

    private fun speakOut(text: String?) {
        textToSpeech!!.speak(text, TextToSpeech.QUEUE_FLUSH, null)
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