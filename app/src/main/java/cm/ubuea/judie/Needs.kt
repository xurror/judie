package cm.ubuea.judie

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import cm.ubuea.judie.MainActivity
import cm.ubuea.judie.Needs

class Needs : AppCompatActivity() {
    private var submit: Button? = null
    private var etName: EditText? = null
    private var etContact: EditText? = null
    private var etAdd: EditText? = null
    private var etDob: EditText? = null
    private var etEmer: EditText? = null
    private var vImpaired: CheckBox? = null
    var name: String? = null
    var address: String? = null
    var phone: String? = null
    var data: String? = null
    var birth: String? = null
    var vImp = "no"
    var emer: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_needs)


        //Grabbing References
        submit = findViewById<View>(R.id.buttonSubmit) as Button
        etName = findViewById<View>(R.id.editTextName) as EditText
        etAdd = findViewById<View>(R.id.editTextAdd) as EditText
        etContact = findViewById<View>(R.id.editTextContact) as EditText
        etDob = findViewById<View>(R.id.editTextdob) as EditText
        vImpaired = findViewById<View>(R.id.checkBoxYes) as CheckBox
        etEmer = findViewById<View>(R.id.et_emergency) as EditText
        submit!!.setOnClickListener { //Initializing variables
            name = etName!!.text.toString()
            address = etAdd!!.text.toString()
            phone = etContact!!.text.toString()
            birth = etDob!!.text.toString()
            if (vImpaired!!.isChecked) {
                vImp = "yes"
            }
            emer = etEmer!!.text.toString()
            //Saves to database
            try {
                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(baseContext)
                val editor = sharedPreferences.edit()
                editor.putString(NAME, name)
                editor.putString(ADD, address)
                editor.putString(PHno, phone)
                editor.putString(BIRTH, birth)
                editor.putString(VIMP, vImp)
                editor.putString(EMER, emer)
                editor.commit()
                /*
                    AayaDatabase enterDatabase = new AayaDatabase(Needs.this);
                    enterDatabase.open();
                    enterDatabase.createEntry(name, address, phone, birth, vImp);
                    data = enterDatabase.getData();
                    enterDatabase.close();
                    */
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                //Toast.makeText(Needs.this, data, Toast.LENGTH_LONG).show();
            }
            val intent = Intent(this@Needs, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    companion object {
        const val NAME = "name"
        const val ADD = "add"
        const val PHno = "phno"
        const val DATA = "data"
        const val BIRTH = "birth"
        const val VIMP = "vimp"
        const val EMER = "emer"
    }
}