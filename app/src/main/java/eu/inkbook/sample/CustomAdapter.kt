package eu.inkbook.sample

import android.R
import android.content.Context
import android.database.DataSetObserver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.TextView
import org.w3c.dom.Text


class CustomAdapter(val context: Context?, val arrayList: ArrayList<DataEntity>?) : ListAdapter {

    override fun areAllItemsEnabled(): Boolean {
        return false
    }

    override fun isEnabled(position: Int): Boolean {
        return true
    }

    override fun registerDataSetObserver(observer: DataSetObserver?) {}
    override fun unregisterDataSetObserver(observer: DataSetObserver?) {}
    override fun getCount(): Int {
        return arrayList!!.size
    }

    override fun getItem(position: Int): Any? {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return false
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
            date.setText("${dataEntity.dateRep}: ${dataEntity.countriesAndTerritories} ${dataEntity.Cumulative_number}")
            convertView =date;
        }
        return convertView
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getViewTypeCount(): Int {
        return arrayList!!.size
    }

    override fun isEmpty(): Boolean {
        return false
    }

}