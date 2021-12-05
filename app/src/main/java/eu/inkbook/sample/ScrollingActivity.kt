package eu.inkbook.sample

import android.app.DownloadManager
import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.*


class ScrollingActivity : AppCompatActivity() {
    private var downloadReceiver: DownloadsBroadcastReceiver? = null
    private val intentFilter = IntentFilter().apply {
        addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
//        addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED)
    }

    companion object {
        val TAG = ScrollingActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.e(TAG, "onCreate")

        setContentView(R.layout.activity_scrolling)
        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout).title = title

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            if (isOnline()) {
                downloadCsvDataFile(
                    this@ScrollingActivity,
                    "https://opendata.ecdc.europa.eu/covid19/casedistribution/csv",
                    "Pobieram CSV"
                )
            } else {
                Toast.makeText(
                    applicationContext,
                    resources.getString(R.string.you_offline),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "registerReceiver")
        if(downloadReceiver == null) downloadReceiver = DownloadsBroadcastReceiver(this)
        registerReceiver(downloadReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(downloadReceiver)
        downloadReceiver = null
        Log.e(TAG, "unregisterReceiver")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_scrolling, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun isExternalStorageWritableAndReadable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    private fun downloadCsvDataFile(baseActivity: Context, url: String?, title: String?): Long {
        val direct = baseActivity.getExternalFilesDir(null)

        // Check if external storage available and can permit r/w operations
        if (isExternalStorageWritableAndReadable() && direct != null) {
            Log.e(TAG, "Directory: ${direct.absolutePath}")
            val extension = "csv"
            val dm = baseActivity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(url)
            val request = DownloadManager.Request(uri)

            try {
                request.setDestinationInExternalFilesDir(
                    baseActivity,
                    null,
                    "covid.$extension"
                )
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setTitle(title)

                Toast.makeText(baseActivity, "Start downloading..", Toast.LENGTH_SHORT).show()

                return dm.enqueue(request)
            } catch (e: IllegalStateException) {
                Toast.makeText(
                    baseActivity,
                    "The external storage directory cannot be found or created",
                    Toast.LENGTH_LONG
                ).show()
                return -1L
            }
        } else {
            Toast.makeText(baseActivity, "External storage is inaccessible", Toast.LENGTH_LONG)
                .show()
            return -1L
        }
    }

    fun updateListView(arrayList: ArrayList<DataEntity>) {
        val list: ListView = findViewById(R.id.list)

        val customAdapter = CustomAdapter(this, arrayList)
        list.adapter = customAdapter
        print(arrayList)
    }

    private fun isOnline(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return networkInfo?.isConnected == true
    }

    fun Read_File(context: Context, filename: String?): String? {
        return try {
            val fis: FileInputStream = context.openFileInput(filename)
            val isr = InputStreamReader(fis, "UTF-8")
            val bufferedReader = BufferedReader(isr)
            val sb = StringBuilder()
            var line: String = ""
            while (bufferedReader.readLine().also({ line = it }) != null) {
                sb.append(line).append("\n")
            }
            sb.toString()
        } catch (e: FileNotFoundException) {
            null
        } catch (e: UnsupportedEncodingException) {
            null
        } catch (e: IOException) {
            null
        }
    }
}