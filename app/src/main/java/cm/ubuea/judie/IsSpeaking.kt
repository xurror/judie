package cm.ubuea.judie

import android.speech.tts.TextToSpeech
import androidx.fragment.app.DialogFragment

class IsSpeaking(var textToSpeech: TextToSpeech, var d: DialogFragment) : Thread() {
    override fun run() {
        super.run()
        do {
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        } while (textToSpeech.isSpeaking)
        d.dismiss()
    }

}