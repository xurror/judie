package cm.ubuea.judie2

import android.app.Application

class Judie: Application() {
    companion object {
        lateinit var user: String
        const val botUser = "Judie"
    }
}