package cm.ubuea.judie

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import cm.ubuea.judie.Commands.filterCommands
import cm.ubuea.judie.PhoneModule
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class PhoneModule : Activity(), OnInitListener {
    var mProgress: ProgressDialog? = null
    var extras: String? = ""
    var check: String? = null
    var m: TextView? = null
    var dateView: TextView? = null
    var tts: TextToSpeech? = null
    var COMMAND: String? = ""
    var b: ImageButton? = null
    var hlp: Button? = null
    var number: String? = null
    var date: String? = null
    val SMS_CODE = 10001
    val REQ_CODE = 100
    var callIntent: Intent? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.phone_module)
        mProgress = ProgressDialog(this)
        m = findViewById<View>(R.id.textViewShowPhone) as TextView
        mProgress!!.setMessage("Fetching ....")
        hlp = findViewById<View>(R.id.buttonHelpPhone) as Button
        tts = TextToSpeech(this, this)
        b = findViewById<View>(R.id.imageButtonSpeakPhone) as ImageButton
        dateView = findViewById<View>(R.id.textView7datephone) as TextView
        val df: DateFormat = SimpleDateFormat("EEE, d MMM yyyy, h:mm a")
        date = df.format(Calendar.getInstance().time)
        dateView!!.text = date
        b!!.setOnClickListener { promptSpeechInput() }
        hlp!!.setOnClickListener {
            MainActivity.module = "phone"
            val frag = HelpFrag()
            frag.show(frag.fragmentManager!!, null)
        }
    }

    //Which Functionality
    private fun launchModule() {
        if (COMMAND == Commands.CALL) call() else if (COMMAND == Commands.SMS_SEND) {
            val intent = Intent(this@PhoneModule, ComposeMail::class.java)
            startActivityForResult(intent, SMS_CODE)
        } else if (COMMAND == Commands.PHONE_HELP) {
            MainActivity.module = "phone"
            val frag = HelpFrag()
            frag.show(frag.fragmentManager!!, null)
        } else myTask().execute(COMMAND)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_CODE -> {
                if (resultCode == RESULT_OK && null != data) {
                    val result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    val a = filterCommands(result[0])
                    COMMAND = a[0]
                    extras = a[1]
                    check = a[2]
                    m!!.text = "$COMMAND $extras"
                    launchModule()
                }
            }
            SMS_CODE -> if (resultCode == RESULT_OK && data != null) {
                val body = """
                    Subject${data.getStringExtra(Commands.MAIL_SUBJECT)}
                    ${data.getStringExtra(Commands.MAIL_BODY)}
                    """.trimIndent()
                val to = data.getStringExtra(Commands.MAIL_TO)
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(to, null, body, null, null)
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private fun call(): String? {
        if (check == null) {
            callIntent = Intent(Intent.ACTION_CALL)
            callIntent!!.data = Uri.parse("tel:$extras")
            startActivity(callIntent)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE),  //request specific permission from user
                        10)
                return null
            }
        } else {
            val uri = ContactsContract.CommonDataKinds.Contactables.CONTENT_URI
            val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER)
            val selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like'%" + extras + "%'"
            val people = contentResolver.query(uri, projection, selection, null, ContactsContract.Contacts.SORT_KEY_PRIMARY)
            val indexNumber = people!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            people.moveToFirst()
            try {
                do {
                    number = people.getString(indexNumber)
                    if (number!!.contains("+91")) {
                        number = number!!.replace("+91", "")
                    }
                } while (people.moveToNext() && people.position != 1)
            } catch (ex: Exception) {
                Toast.makeText(this, "Sorry no such contact", Toast.LENGTH_SHORT)
            }
            callIntent = Intent(Intent.ACTION_CALL)
            callIntent!!.data = Uri.parse("tel:$number")
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null
            }
        }
        if (number != null) {
            startActivity(callIntent)
        }
        return null
    }

    /*
    private void get(Cursor cursor , String... name)
    {
        List<Integer> idx = new ArrayList<>();
        for(String n : name)
        {
            for (int i = 0; i < cursor.getCount();i++)
            {
                if(n.equals(cursor.get))
            }
        }
    }
*/
    internal inner class myTask : AsyncTask<String?, Void?, String?>() {
        var n: String? = null
        override fun onPreExecute() {
            super.onPreExecute()
            mProgress!!.show()
        }

        protected override fun doInBackground(vararg params: String?): String? {
            when (params[0]) {
                Commands.CALL_LOG -> n = callLogs()
                Commands.SMS -> n = sms()
                Commands.CALL -> n = call()
                Commands.CONTACTS -> n = contacts()
                Commands.SMS_SEND -> sendMessage()
                else -> n = "Invalid Command Please say help for the commands"
            }
            return n
        }

        //Send Message
        fun sendMessage() {
            val i = Intent(this@PhoneModule, ComposeMail::class.java)
            startActivityForResult(i, SMS)
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
        private fun contacts(): String {
            var nameall = ""
            val uri = ContactsContract.CommonDataKinds.Contactables.CONTENT_URI
            val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER)
            val selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " like'%" + extras + "%'"
            val people = contentResolver.query(uri, projection, selection, null, ContactsContract.Contacts.SORT_KEY_PRIMARY)
            val indexName = people!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            people.moveToFirst()
            try {
                do {
                    val name = people.getString(indexName)
                    var numberr = people.getString(indexNumber)
                    if (numberr.contains("+91")) {
                        numberr = numberr.replace("+91", "0")
                    }
                    nameall = """
                        $nameall$name
                        $numberr




                        """.trimIndent()
                    // Do work...
                } while (people.moveToNext() && people.position != 5)
            } catch (ex: Exception) {
                nameall = "Sorry no such contact found"
            }
            return nameall
        }

        private fun callLogs(): String {
            val c = contentResolver.query(Uri.parse("content://call_log/calls"), null, null, null, CallLog.Calls.DATE + " DESC")
            val d = SimpleDateFormat("EEE, d MMM yyyy, HH:mm")
            var details = ""
            if (c!!.moveToFirst()) {
                do {
                    var num = c.getString(c.getColumnIndex(CallLog.Calls.NUMBER)) // for  number
                    if (num.contains("+91")) {
                        num = num.replace("+91", "0")
                    }
                    val date = Date(c.getLong(c.getColumnIndex("date")))
                    var name = c.getString(c.getColumnIndex(CallLog.Calls.CACHED_NAME)) // for name
                    if (name == null) name = "unknown"
                    val t = c.getString(c.getColumnIndex(CallLog.Calls.DURATION)).toLong()
                    var duration = ""
                    duration += (t / 60).toString() + " minutes "
                    duration += (t % 60).toString() + " seconds "
                    val type = c.getString(c.getColumnIndex(CallLog.Calls.TYPE)).toInt()
                    details = """
                        $details$num
                        $name
                        $duration
                        ${d.format(date)}



                        """.trimIndent()
                } while (c.moveToNext() && c.position != 10)
            }
            return details
        }

        private fun sms(): String {
            val d = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss")
            val cursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null)
            var msgData = ""
            var i = 0
            if (cursor!!.moveToFirst()) { // must check the result to prevent exception
                do {
                    msgData += """ From:   ${cursor.getString(cursor.getColumnIndex("address"))} """
                    val date = Date(cursor.getLong(cursor.getColumnIndex("date")))
                    msgData += """ Date:   ${d.format(date)} """
                    msgData += """ Body:   ${cursor.getString(cursor.getColumnIndex("body"))} """
                    msgData = """ $msgData """.trimIndent()
                    i++
                } while (cursor.moveToNext() && i < 5)
            } else {
                // empty box, no SMS
            }
            return msgData
        }

        override fun onPostExecute(s: String?) {
            super.onPostExecute(s)
            mProgress!!.hide()
            result = n
            val n = PhoneFragment()
            n.show(n.fragmentManager!!, "ss")
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

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported")
            } else {
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

    companion object {
        var result: String? = null
        const val SMS = 111111111
    }
}