package cm.ubuea.judie

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*

class DataBaseNotes internal constructor(private val dbContext: Context) {
    private object Constants {
        const val KEY_ID = "ID"
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val KEY_PRIORITY = "priority"
        const val DATABASE_NAME = "judie"
        const val TABLENAME = "j_notes"
        const val VERSION = 1

    }
    private val s = "create table " + Constants.TABLENAME + " (" +
            Constants.KEY_ID + " integer PRIMARY KEY autoincrement DEFAULT 0, " +
            Constants.KEY_TITLE + " text, " +
            Constants.KEY_PRIORITY + " text, " +
            Constants.KEY_BODY + " text);"
    private var dbHelper: DbHelper? = null
    private var db: SQLiteDatabase? = null

    internal inner class DbHelper(context: Context?) : SQLiteOpenHelper(context, Constants.DATABASE_NAME, null, Constants.VERSION) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(s)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE $Constants.TABLENAME")
            onCreate(db)
        }
    }

    @Throws(SQLException::class)
    fun createEntry(title: String?, body: String?, priority: String?) {
        dbHelper = DbHelper(dbContext)
        db = dbHelper!!.writableDatabase
        val cv = ContentValues()
        cv.put(Constants.KEY_BODY, body)
        cv.put(Constants.KEY_PRIORITY, priority)
        cv.put(Constants.KEY_TITLE, title)
        db!!.insertOrThrow(Constants.TABLENAME, null, cv)
        dbHelper!!.close()
    }

    val rOWs: List<SingleRow?>
        @SuppressLint("Recycle")
        get() {
            dbHelper = DbHelper(dbContext)
            db = dbHelper!!.writableDatabase
            val list: MutableList<SingleRow?> = ArrayList()
            val columns = arrayOf(Constants.KEY_ID, Constants.KEY_BODY, Constants.KEY_TITLE, Constants.KEY_PRIORITY)
            val c = db!!.query(Constants.TABLENAME, columns, null, null, null, null, null)
            var ID: String? = null
            var title: String? = null
            var body: String? = null
            var priority: String? = null
            while (c.moveToNext()) {
                ID = c.getInt(0).toString()
                body = c.getString(1)
                title = c.getString(2)
                priority = c.getString(3)
                list.add(SingleRow(ID, priority, title, body))
            }
            dbHelper!!.close()
            if (list[0] == null) list.add(SingleRow("0", "0", "no item", ""))
            return list
        }

}