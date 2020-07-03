package cm.ubuea.judie

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.appindexing.AppIndex
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.Base64
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import com.google.api.services.gmail.model.ListMessagesResponse
import com.google.api.services.gmail.model.Message
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class GmailModule : Activity(), PermissionCallbacks, View.OnClickListener, OnInitListener {
    var mCredential: GoogleAccountCredential? = null
    var mProgress: ProgressDialog? = null
    private var tv_show: TextView? = null
    private var dateView: TextView? = null
    private var b_listen: ImageButton? = null
    private var b_help: Button? = null

    //voice related Var
    private var tts: TextToSpeech? = null
    var date: String? = null
    var commands: String? = null
    var extras = ""
    var extras_subject: String? = null
    var extras_to: String? = null
    var extras_body: String? = null
    var context: Context? = null

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private var client: GoogleApiClient? = null

    /**
     * Create the main activity.
     *
     * @param savedInstanceState previously saved instance data.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.gmail_module)
        mProgress = ProgressDialog(this)
        mProgress!!.setMessage("Calling Gmail API ...")

        // Initialize credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                applicationContext, Arrays.asList(*SCOPES))
                .setBackOff(ExponentialBackOff())
        val df: DateFormat = SimpleDateFormat("EEE, d MMM yyyy, h:mm a")
        date = df.format(Calendar.getInstance().time)
        init()

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = GoogleApiClient.Builder(this).addApi(AppIndex.API).build()
    }

    private fun init() {
        tv_show = findViewById<View>(R.id.textViewShowGmail) as TextView
        b_listen = findViewById<View>(R.id.imageButtonSpeakGmail) as ImageButton
        b_help = findViewById<View>(R.id.buttonHelpGmail) as Button
        b_listen!!.setOnClickListener(this)
        b_help!!.setOnClickListener(this)
        tts = TextToSpeech(this, this)
        dateView = findViewById<View>(R.id.textView7DateGmail) as TextView
        dateView!!.text = date
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.imageButtonSpeakGmail -> promptSpeechInput()
            R.id.buttonHelpGmail -> {
                MainActivity.module = "gmail"
                Toast.makeText(baseContext, "Help Module", Toast.LENGTH_SHORT).show()
                val frag = HelpFrag()
                frag.show(frag.fragmentManager!!, null)
            }
        }
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private val MAIL = 10101
    private val resultsFromApi: Unit
        private get() {
            if (!isGooglePlayServicesAvailable) {
                acquireGooglePlayServices()
            } else if (!isDeviceOnline) {
                mOutputText = "No network connection available."
            } else {
                chooseAccount()
                when (commands) {
                    Commands.MAIL_COMPOSE_MAIL -> {
                        val i = Intent(this, ComposeMail::class.java)
                        startActivityForResult(i, MAIL)
                    }
                    Commands.MAIL_SEND -> SendTask(mCredential).execute()
                    Commands.helpModule -> {
                        MainActivity.module = "gmail"
                        Toast.makeText(baseContext, "Help Module", Toast.LENGTH_SHORT).show()
                        val frag = HelpFrag()
                        frag.show(frag.fragmentManager!!, null)
                        MakeRequestTask(mCredential).execute(commands, extras)
                    }
                    else -> MakeRequestTask(mCredential).execute(commands, extras)
                }
            }
        }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private fun chooseAccount() {
        if (EasyPermissions.hasPermissions(
                        this, Manifest.permission.GET_ACCOUNTS)) {
            val accountName = getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null)
            if (accountName != null) {
                mCredential!!.selectedAccountName = accountName
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential!!.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER)
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS)
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     * activity result.
     * @param data Intent (containing result data) returned by incoming
     * activity result.
     */
    var name: String? = null
    override fun onActivityResult(
            requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != Activity.RESULT_OK) {
                mOutputText = "This app requires Google Play Services. Please install " +
                        "Google Play Services on your device and relaunch this app."
            } else {
                resultsFromApi
            }
            REQUEST_ACCOUNT_PICKER -> if (resultCode == Activity.RESULT_OK && data != null && data.extras != null) {
                val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                // name = data.getStringExtra(AccountManager.KEY_USERDATA);
                if (accountName != null) {
                    val settings = getPreferences(Context.MODE_PRIVATE)
                    val editor = settings.edit()
                    editor.putString(PREF_ACCOUNT_NAME, accountName)
                    //editor.putString(PREF_NAME,name);
                    editor.apply()
                    mCredential!!.selectedAccountName = accountName
                    resultsFromApi
                }
            }
            REQUEST_AUTHORIZATION -> if (resultCode == Activity.RESULT_OK) {
                resultsFromApi
            }
            REQ_CODE -> if (resultCode == Activity.RESULT_OK && null != data) {
                val result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val text_voice = result[0]
                tv_show!!.text = text_voice
                try {
                    Thread.sleep(1000)
                    speakOut(tv_show!!.text.toString())
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                val a = Commands.filterCommands(text_voice)
                commands = a[0]
                extras = a[1].toString()
                resultsFromApi
            }
            MAIL -> if (resultCode == Activity.RESULT_OK) {
                extras_to = data!!.getStringExtra(Commands.MAIL_TO)
                extras_body = data.getStringExtra(Commands.MAIL_BODY)
                extras_subject = data.getStringExtra(Commands.MAIL_SUBJECT)
                commands = Commands.MAIL_SEND
                resultsFromApi
            }
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     * requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     * which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    override fun onRequestPermissionsResult(requestCode: Int,
                                            @NonNull permissions: Array<String>,
                                            @NonNull grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this)
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     * permission
     * @param list        The requested permission list. Never null.
     */
    override fun onPermissionsGranted(requestCode: Int, list: List<String>) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     * permission
     * @param list        The requested permission list. Never null.
     */
    override fun onPermissionsDenied(requestCode: Int, list: List<String>) {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private val isDeviceOnline: Boolean
        private get() {
            val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connMgr.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private val isGooglePlayServicesAvailable: Boolean
        private get() {
            val apiAvailability = GoogleApiAvailability.getInstance()
            val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
            return connectionStatusCode == ConnectionResult.SUCCESS
        }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }

    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     * Google Play Services on this device.
     */
    fun showGooglePlayServicesAvailabilityErrorDialog(
            connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
                this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES)
        dialog.show()
    }

    // voice related Code (BEGINING)
    private val REQ_CODE = 100
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

    private fun speakOut(text: String) {
        tts!!.speak(text, TextToSpeech.QUEUE_FLUSH, null)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tv_show!!.text = "TTS  This Language is not supported"
            } else {
                //bl.setEnabled(true);
                speakOut(tv_show!!.text.toString())
            }
        } else {
            tv_show!!.text = "TTS Initilization Failed!"
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

    //  ASYNC_TASK CLASS
    private inner class MakeRequestTask(credential: GoogleAccountCredential?) : AsyncTask<String?, Void?, List<String?>?>() {
        private var mService: Gmail? = null
        private var mLastError: Exception? = null
        private val number_of_msgs: Long = 3

        /**
         * Background task to call Gmail API.
         *
         * @param params no parameters needed for this task.
         */
        override fun doInBackground(vararg params: String?): List<String?>? {
            return try {
                when (params[0]) {
                    Commands.MAIL_FETCH_MAILS -> getMessage(Commands.MAIL_FETCH_MAILS)
                    Commands.MAIL_FEtCH_LABELS -> {
                        val n: MutableList<String?> = ArrayList()
                        n.add("INBOX")
                        n.add("UNREAD")
                        n.add("CATEGORY_PERSONAL")
                        n
                    }
                    Commands.MAIL_SEARCH_SUBJECT -> getMessage(Commands.MAIL_SEARCH_SUBJECT)
                    Commands.MAIL_SEARCH_LABELS -> {
                        extras = "INBOX"
                        extras = if (extras === "INBOX") "INBOX" else if (extras === "CATEGORY_SOCIAL") "CATEGORY_SOCIAL" else if (extras === "UNREAD") "UNREAD" else {
                            "ALL"
                        }
                        getMessage(Commands.MAIL_FETCH_MAILS)
                    }
                    else -> null
                }
            } catch (e: Exception) {
                mLastError = e
                cancel(true)
                null
            }
        }

        private fun getheader(list: Message, vararg p: String): IntArray {
            val index = IntArray(p.size)
            for (i in list.payload.headers.indices) {
                if (list.payload.headers[i].name.toString() == p[0]) {
                    print(i.toString() + list.payload.headers[i].toString())
                    index[0] = i
                }
                if (list.payload.headers[i].name.toString() == p[1]) {
                    print(i.toString() + list.payload.headers[i].toString())
                    index[1] = i
                }
                if (list.payload.headers[i].name.toString() == p[2]) {
                    print(i.toString() + list.payload.headers[i].toString())
                    index[2] = i
                }
            }
            return index
        }

        //get messages, subjects, body using message id
        @Throws(IOException::class)
        fun getMessage(type: String?): List<String?> {
            val messages: MutableList<String?> = ArrayList()
            val list = listMessagesWithLabels(mService, "me", type)
            for (msg in list!!) {

                //   messages.add(getPreferences(Context.MODE_PRIVATE).getString(PREF_NAME,null));
                val message = mService!!.users().messages()["me", msg.id].setFormat("full").setMetadataHeaders(messages).execute()
                val n = getheader(message, "Subject", "Date", "From")
                messages.add(message.payload.headers[n[0]].name + "    " + message.payload.headers[n[0]].value)
                messages.add(message.payload.headers[n[1]].name + "    " + message.payload.headers[n[1]].value)
                messages.add(message.payload.headers[n[2]].name + "    " + message.payload.headers[n[2]].value)
                messages.add(""" Snippet     ${message.snippet} """.trimIndent())
            }
            return messages
        }

        var next: String? = null

        // returns message id(s)
        @Throws(IOException::class)
        fun listMessagesWithLabels(service: Gmail?, userId: String?, type: String?): List<Message>? {
            val response: ListMessagesResponse
            val messages: MutableList<Message>
            val labelIds: MutableList<String>
            return when (type) {
                Commands.MAIL_SEARCH_SUBJECT -> {
                    labelIds = ArrayList()
                    //labelIds.add("CATEGORY_PERSONAL");
                    response = mService!!.users().messages().list("me").setLabelIds(labelIds).setQ("subject:$extras").execute()
                    messages = ArrayList()
                    next = response.nextPageToken
                    messages.addAll(response.messages)
                    messages
                }
                Commands.MAIL_FETCH_MAILS -> {
                    labelIds = ArrayList()
                    if (extras === "INBOX") labelIds.add("INBOX") else if (extras === "CATEGORY_SOCIAL") labelIds.add("CATEGORY_SOCIAL") else if (extras === "UNREAD") labelIds.add("UNREAD") else {
                        // labelIds.add("INBOX");
                        //labelIds.add("CATEGORY_SOCIAL");
                        // labelIds.add("UNREAD");
                    }
                    response = mService!!.users().messages().list("me").setLabelIds(labelIds).setMaxResults(number_of_msgs).execute()
                    next = response.nextPageToken
                    messages = ArrayList()
                    messages.addAll(response.messages)
                    messages
                }
                Commands.MAIL_NEXT -> {
                    response = mService!!.users().messages().list("me").setQ("subject:" + "Hello").setPageToken(next).execute()
                    messages = ArrayList()
                    messages.addAll(response.messages)
                    messages
                }
                else -> null
            }
        }// Get the labels in the user's account.

        /**
         * Fetch a list of Gmail labels attached to the specified account.
         *
         * @return List of Strings labels.
         * @throws IOException
         */
        @get:Throws(IOException::class)
        private val dataFromApi: List<String>
            private get() {

                // Get the labels in the user's account.
                val user = "me"
                val labels: MutableList<String> = ArrayList()
                val listResponse = mService!!.users().labels().list(user).execute()
                for (label in listResponse.labels) {
                    labels.add(label.id)
                }
                return labels
            }

        override fun onPreExecute() {
            mOutputText = ""
            mProgress!!.show()
        }

        override fun onPostExecute(result: List<String?>?) {
            mProgress!!.hide()
            if (result == null || result.size == 0) {
                mOutputText = "No results returned."
            } else {
                result.toMutableList().add(0, "Mails From Gmail\n")
                mOutputText = TextUtils.join("\n", result)
                val m = DisplayMsgs()
                m.show(m.fragmentManager!!, null)
            }
        }

        override fun onCancelled() {
            mProgress!!.hide()
            if (mLastError != null) {
                if (mLastError is GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            (mLastError as GooglePlayServicesAvailabilityIOException)
                                    .connectionStatusCode)
                } else if (mLastError is UserRecoverableAuthIOException) {
                    startActivityForResult(
                            (mLastError as UserRecoverableAuthIOException).intent,
                            REQUEST_AUTHORIZATION)
                } else {
                    mOutputText = """
                        The following error occurred:
                        ${mLastError!!.message}
                        """.trimIndent()
                }
            } else {
                mOutputText = "Request cancelled."
            }
        }

        init {
            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
            mService = Gmail.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Gmail API Android Quickstart")
                    .build()
        }

    }

    private inner class SendTask(credential: GoogleAccountCredential?) : AsyncTask<String?, Void?, Void?>() {
        private var mService: Gmail? = null
        private var mLastError: Exception? = null

        /**
         * Background task to call Gmail API.
         *
         * @param params no parameters needed for this task.
         */
        protected override fun doInBackground(vararg params: String?): Void? {
            return try {
                sendMail(extras_to, "me", extras_subject, extras_body)
                null
            } catch (e: Exception) {
                mLastError = e
                e.printStackTrace()
                cancel(true)
                null
            }
        }

        @Throws(MessagingException::class, IOException::class)
        fun sendMail(sender: String?, receiver: String?, subject: String?, content: String?) {
            val msg = createEmail(sender, receiver, subject, content)
            sendMessage(mService, "me", msg)
        }

        /**
         * Create a MimeMessage using the parameters provided.
         *
         * @param to       Email address of the receiver.
         * @param from     Email address of the sender, the mailbox account.
         * @param subject  Subject of the email.
         * @param bodyText Body text of the email.
         * @return MimeMessage to be used to send email.
         * @throws MessagingException
         */
        @Throws(MessagingException::class)
        fun createEmail(to: String?, from: String?, subject: String?,
                        bodyText: String?): MimeMessage {
            val props = Properties()
            val session = Session.getDefaultInstance(props, null)
            val email = MimeMessage(session)
            val tAddress = InternetAddress(to)
            val fAddress = InternetAddress(from)
            email.setFrom(InternetAddress(from))
            email.addRecipient(javax.mail.Message.RecipientType.TO,
                    InternetAddress(to))
            email.subject = subject
            email.setText(bodyText)
            return email
        }

        /**
         * Create a Message from an email
         *
         * @param email Email to be set to raw of message
         * @return Message containing base64url encoded email.
         * @throws IOException
         * @throws MessagingException
         */
        @Throws(MessagingException::class, IOException::class)
        fun createMessageWithEmail(email: MimeMessage): Message {
            val bytes = ByteArrayOutputStream()
            email.writeTo(bytes)
            val encodedEmail = Base64.encodeBase64URLSafeString(bytes.toByteArray())
            val message = Message()
            message.raw = encodedEmail
            return message
        }

        /**
         * Send an email from the user's mailbox to its recipient.
         *
         * @param service Authorized Gmail API instance.
         * @param userId  User's email address. The special value "me"
         * can be used to indicate the authenticated user.
         * @param email   Email to be sent.
         * @throws MessagingException
         * @throws IOException
         */
        @Throws(MessagingException::class, IOException::class)
        fun sendMessage(service: Gmail?, userId: String?, email: MimeMessage) {
            var message = createMessageWithEmail(email)
            message = service!!.users().messages().send(userId, message).execute()
            println("Message id: " + message.id)
            println(message.toPrettyString())
        }

        /**
         * Fetch a list of Gmail labels attached to the specified account.
         *
         * @return List of Strings labels.
         * @throws IOException
         */
        override fun onPreExecute() {
            mOutputText = ""
            mProgress!!.show()
        }

        override fun onPostExecute(aVoid: Void?) {
            super.onPostExecute(aVoid)
            mProgress!!.hide()
        }

        override fun onCancelled() {
            mProgress!!.hide()
            if (mLastError != null) {
                if (mLastError is GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            (mLastError as GooglePlayServicesAvailabilityIOException)
                                    .connectionStatusCode)
                } else if (mLastError is UserRecoverableAuthIOException) {
                    startActivityForResult(
                            (mLastError as UserRecoverableAuthIOException).intent,
                            REQUEST_AUTHORIZATION)
                } else {
                    mOutputText = """
                        The following error occurred:
                        ${mLastError!!.message}
                        """.trimIndent()
                }
            } else {
                mOutputText = "Request cancelled."
            }
        }

        init {
            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
            mService = Gmail.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Gmail API Android Quickstart")
                    .build()
        }
    }

    companion object {
        @JvmField
        var mOutputText: String? = null

        // Gmial Related Var
        const val REQUEST_ACCOUNT_PICKER = 1000
        const val REQUEST_AUTHORIZATION = 1001
        const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
        const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
        private const val PREF_ACCOUNT_NAME = "accountName"
        private const val PREF_NAME = "accountName"
        private val SCOPES = arrayOf(GmailScopes.GMAIL_LABELS, GmailScopes.GMAIL_READONLY, GmailScopes.GMAIL_MODIFY)
    }
}