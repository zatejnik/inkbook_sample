package eu.inkbook.sample.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import eu.inkbook.sample.data.DataEntity


class CustomAdapter(private val context: Context?, val arrayList: ArrayList<DataEntity>?) : BaseAdapter() {

    override fun getCount(): Int {
        return arrayList?.size ?: 0
    }

    override fun getItem(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View? {
        var convertView = convertView
        val dataEntity: DataEntity = arrayList!![position]
        if (convertView == null) {
//            val layoutInflater = LayoutInflater.from(context)
//            convertView = layoutInflater.inflate(R.layout.list_row, null)
//            convertView!!.setOnClickListener { }
//            val tittle = convertView.findViewById<TextView>(R.id.title)
//            val imag: ImageView = convertView.findViewById(R.id.list_image)
//            tittle.setText(subjectData.SubjectName)
            val date = TextView(context)
            date.text = "${dataEntity.dateRep}: ${dataEntity.countriesAndTerritories} ${dataEntity.Cumulative_number}"
            convertView =date;
        }
        return convertView
    }
}