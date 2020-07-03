package cm.ubuea.judie

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler

class Splash : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler().postDelayed({
            val pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE)
            if (pref.getBoolean("activity_executed", false)) {
                val intent = Intent(this@Splash, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val ed = pref.edit()
                ed.putBoolean("activity_executed", true)
                ed.commit()
                //Starts needs activity
                val intent = Intent(this@Splash, Needs::class.java)
                startActivity(intent)
            }


            //closes splash
            finish()
        }, SPLASH_TIMER_COUNT.toLong())
    }

    companion object {
        private const val SPLASH_TIMER_COUNT = 5000
    }
}