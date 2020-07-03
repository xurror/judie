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

class DisplayFrag : DialogFragment(), OnInitListener {
    var textToSpeech: TextToSpeech? = null
    var tv_dispay: TextView? = null
    var i: IsSpeaking? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.dis_frag, null)
        tv_dispay = view.findViewById<View>(R.id.tv_dis) as TextView
        tv_dispay!!.text = arguments!!.getString(Commands.DATE)
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
                tv_dispay!!.text = "TTS  This Language is not supported"
            } else {
                //bl.setEnabled(true);
                speakOut(arguments!!.getString(Commands.DATE))
                i!!.start()
                // dismiss();
            }
        } else {
            tv_dispay!!.text = "TTS Initilization Failed!"
        }
    }

    private fun speakOut(text: String?) {
        textToSpeech!!.speak(text, TextToSpeech.QUEUE_FLUSH, null)
    }
}