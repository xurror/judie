package cm.ubuea.judie

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class MyAdapter(var context: Context) : BaseAdapter() {
    override fun getCount(): Int {
        return try {
            list!!.size
        } catch (e: Exception) {
            0
        }
    }

    override fun getItem(position: Int): SingleRow? {
        return list!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.row, null)
        val tv_title = view.findViewById<View>(R.id.n_title) as TextView
        val tv_body = view.findViewById<View>(R.id.n_body) as TextView
        val iv_p = view.findViewById<View>(R.id.n_pri) as ImageView
        val sr = list!![position]
        tv_title.text = sr!!.title
        tv_body.text = sr.body
        when (sr.priority) {
            "1" -> iv_p.setImageResource(R.drawable.h)
            "2" -> iv_p.setImageResource(R.drawable.m)
            "3" -> iv_p.setImageResource(R.drawable.l)
            else -> iv_p.setImageResource(R.drawable.n)
        }
        return view
    }

    companion object {
        @JvmField
        var list: List<SingleRow?>? = null
        var result = ""
    }

    init {
        val db = DataBaseNotes(context)
        try {
            list = db.rOWs
        } catch (ex: IndexOutOfBoundsException) {
            ex.printStackTrace()
        }
    }
}