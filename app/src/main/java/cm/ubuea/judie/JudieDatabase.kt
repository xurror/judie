package cm.ubuea.judie

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.sql.SQLException

class JudieDatabase //constructor
(private val passedContext: Context) {
    private var helper: DbHelper? = null
    private var database: SQLiteDatabase? = null

    private class DbHelper  //constructor
    (context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        override fun onCreate(db: SQLiteDatabase) {
            val createDatabaseQuery = ("CREATE  TABLE " + TABLE_NAME +
                    " (" + USER_NAME + " VARCHAR, " + USER_ADD + " VARCHAR, " + USER_CONTACT + " VARCHAR, "
                    + USER_DOB + " VARCHAR, " + VISUALLY_IMPAIRED + " VARCHAR)")
            db.execSQL(createDatabaseQuery)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
            onCreate(db)
        }
    }

    //method to open database
    @Throws(SQLException::class)
    fun open(): JudieDatabase {
        helper = DbHelper(passedContext)
        database = helper!!.writableDatabase
        return this
    }

    //method to close database
    fun close() {
        helper!!.close()
    }

    //Creating Entry of row
    fun createEntry(name: String?, address: String?, phone: String?, birth: String?, vImp: String?): Long {
        val cv = ContentValues()
        cv.put(USER_NAME, name)
        cv.put(USER_ADD, address)
        cv.put(USER_CONTACT, phone)
        cv.put(USER_DOB, birth)
        cv.put(VISUALLY_IMPAIRED, vImp)
        return database!!.insert(TABLE_NAME, null, cv)
    }

    val data: String
        get() {
            val columns = arrayOf(USER_NAME, USER_ADD, USER_CONTACT, USER_DOB, VISUALLY_IMPAIRED)
            val c = database!!.query(TABLE_NAME, columns, null, null, null, null, null, null)
            var result = ""
            val iName = c.getColumnIndex(USER_NAME)
            val iAdd = c.getColumnIndex(USER_ADD)
            val iCon = c.getColumnIndex(USER_CONTACT)
            val iDob = c.getColumnIndex(USER_DOB)
            val iVimp = c.getColumnIndex(VISUALLY_IMPAIRED)
            c.moveToFirst()
            while (!c.isAfterLast) {
                result = """$result${c.getString(iName)} ${c.getString(iAdd)} ${c.getString(iCon)} ${c.getString(iDob)} ${c.getString(iVimp)}
"""
                c.moveToNext()
            }
            return result
        }

    companion object {
        //Database Variables
        const val USER_NAME = "users_name"
        const val USER_ADD = "users_address"
        const val USER_CONTACT = "user_contact"
        const val USER_DOB = "user_dateOfBirth"
        const val VISUALLY_IMPAIRED = "YES_NO"
        private const val DATABASE_NAME = "aayaDatabase"
        private const val TABLE_NAME = "users"
        private const val DATABASE_VERSION = 1
    }

}