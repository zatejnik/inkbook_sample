package eu.inkbook.sample

import android.Manifest
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.*


class ScrollingActivity : AppCompatActivity() {

    companion object {
        val TAG = ScrollingActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_scrolling)
        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout).title = title

//        val list: ListView = findViewById(R.id.list)
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            download(
                this.baseContext,
                "https://opendata.ecdc.europa.eu/covid19/casedistribution/csv",
                "Pobieram CSV"
            )
        }

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

    private fun download(baseActivity: Context, url: String?, title: String?): Long {

        val direct = baseActivity.getExternalFilesDir("inkbook")

        Log.e(TAG, "${baseActivity.getExternalFilesDir("inkbook")}")

        if (!direct!!.exists()) {
            direct.mkdirs()
            Log.e(TAG, "dir not exist, making directory: $direct")
        }

        var extension = "csv"
        val downloadReference: Long
        var dm: DownloadManager
        dm = baseActivity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)
        request.setDestinationInExternalPublicDir(
            "/inkbook",
            "covid" + extension
        )
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setTitle(title)
        Toast.makeText(baseActivity, "start Downloading..", Toast.LENGTH_SHORT).show()

        downloadReference = dm?.enqueue(request) ?: 0
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        val downloadReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val downloadId =
                    intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (downloadId == -1L) return

                // query download status
                val cursor: Cursor =
                    dm.query(DownloadManager.Query().setFilterById(downloadId))
                if (cursor.moveToFirst()) {
                    val status: Int =
                        cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {

                        // download is successful
                        val uri: String =
                            cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                        val file = File(Uri.parse(uri).path)
                        val importer = ImportCsvHelper()
                        val arrayList = importer.save(file)

                        val list: ListView = findViewById(R.id.list)

                        val customAdapter = CustomAdapter(context, arrayList)
                        list.setAdapter(customAdapter)
                        print(arrayList)

                    } else {
                        // download is assumed cancelled
                    }
                } else {
                    // download is assumed cancelled
                }
            }
        }

        registerReceiver(downloadReceiver, filter)
        return downloadReference

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