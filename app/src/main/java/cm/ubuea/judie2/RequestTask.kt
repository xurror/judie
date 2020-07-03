package cm.ubuea.judie2

import android.app.Activity
import android.os.AsyncTask
import com.google.cloud.dialogflow.v2.*

class RequestTask(
    activity: Activity,
    session: SessionName,
    sessionsClient: SessionsClient,
    queryInput: QueryInput
) : AsyncTask<Void, Void, DetectIntentResponse>() {

    var activity: Activity? = activity
    private var session: SessionName? = session
    private var sessionsClient: SessionsClient? = sessionsClient
    private var queryInput: QueryInput? = queryInput

    override fun doInBackground(vararg params: Void?): DetectIntentResponse? {
        try {
            val detectIntentRequest = DetectIntentRequest.newBuilder()
                .setSession(session.toString())
                .setQueryInput(queryInput)
                .build()
            return sessionsClient?.detectIntent(detectIntentRequest)
        } catch (e: Exception) {
            e.printStackTrace()
        }


        return null
    }

    override fun onPostExecute(result: DetectIntentResponse?) {
        (activity as MainActivity).onResult(result)
    }
}
