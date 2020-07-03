package cm.ubuea.judie

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.sql.SQLException

class SingleRowReminder(var id: String?, var tittle: String?, var des: String?)

class DataBase internal constructor(private val ourContext: Context) {
    private object Constants {
        const val KEY_ID = "ID"
        const val KEY_TITLE = "title"
        const val KEY_DES = "des"
        const val DATABASE_NAME = "REMINDER"
        const val TABLENAME = "TITLE"
        const val VERSION = 1
    }
    private val s = "create table " + Constants.TABLENAME + " (" +
            Constants.KEY_ID + " integer PRIMARY KEY autoincrement DEFAULT 0, " +
            Constants.KEY_TITLE + " text, " +
            Constants.KEY_DES + " text);"
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
    fun Open(): DataBase {
        dbHelper = DbHelper(ourContext)
        db = dbHelper!!.writableDatabase
        return this
    }

    @Throws(SQLException::class)
    fun close() {
        dbHelper!!.close()
    }

    @Throws(SQLException::class)
    fun createEntry(id: String?, title: String?, desc: String?): Long {
        val cv = ContentValues()
        cv.put(Constants.KEY_ID, id)
        cv.put(Constants.KEY_DES, desc)
        cv.put(Constants.KEY_TITLE, title)
        return db!!.insert(Constants.TABLENAME, null, cv)
    }

    @SuppressLint("Recycle")
    fun getROW(Id: String): SingleRowReminder {
        val columns = arrayOf(Constants.KEY_ID, Constants.KEY_DES, Constants.KEY_TITLE)
        val c = db!!.query(Constants.TABLENAME, columns, "$Constants.KEY_ID = $Id", null, null, null, null)
        var ID: String? = null
        var title: String? = null
        var desc: String? = null
        while (c.moveToNext()) {
            if (!c.isAfterLast) {
                ID = c.getInt(0).toString()
                desc = c.getString(1)
                title = c.getString(2)
            }
        }
        //SingleRowReminder sr = null;
        return SingleRowReminder(ID, title, desc)
    }

}