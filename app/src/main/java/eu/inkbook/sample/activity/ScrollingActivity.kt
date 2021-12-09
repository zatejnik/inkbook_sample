package eu.inkbook.sample.activity

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import eu.inkbook.sample.*
import eu.inkbook.sample.adapter.CustomAdapter
import eu.inkbook.sample.data.DataEntity
import eu.inkbook.sample.helpers.ImportCsvHelper
import eu.inkbook.sample.helpers.PreferencesKeys.LAST_MODIFIED
import eu.inkbook.sample.helpers.PreferencesKeys.LAST_MODIFIED_BUF
import eu.inkbook.sample.helpers.Storage
import eu.inkbook.sample.helpers.FileDownloadHelper
import eu.inkbook.sample.helpers.FileDownloadHelper.EXTENSION
import eu.inkbook.sample.helpers.FileDownloadHelper.FILE_NAME
import eu.inkbook.sample.helpers.FileDownloadHelper.isDownloading
import eu.inkbook.sample.helpers.FileDownloadHelper.getMimeTypeForCsv
import eu.inkbook.sample.helpers.FileDownloadHelper.isExternalStorageWritableAndReadable
import eu.inkbook.sample.helpers.FileDownloadHelper.isOnline
import eu.inkbook.sample.helpers.FileDownloadHelper.removeOldFile
import eu.inkbook.sample.helpers.PreferencesKeys.DOWNLOAD_REF
import kotlinx.coroutines.launch
import java.io.*


class ScrollingActivity : AppCompatActivity() {
    private val TAG = ScrollingActivity::class.java.simpleName
    private val intentFilter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)

    private lateinit var fab: FloatingActionButton
    private lateinit var listView: ListView
    private lateinit var customAdapter: CustomAdapter

    private lateinit var storage: Storage

    private val downloadsBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            fab.isEnabled = true
            val downloadId =
                intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadId == -1L || downloadId == null) return

            // query download status
            val dm = context?.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
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
                    val arrayList = ImportCsvHelper.save(file)
                    updateListView(arrayList)

                    // Save new LAST_MODIFIED value for future comparing
                    val bufLastModified = storage.getLong(LAST_MODIFIED_BUF, 0L)
                    storage.putLong(LAST_MODIFIED, bufLastModified)
                } else {
                    // Set LAST_MODIFIED to 0 so that the user can download the file again(the old one is deleted with removeOldFile())
                    storage.putLong(LAST_MODIFIED, 0L)
                }
            } else {
                // The same as above
                storage.putLong(LAST_MODIFIED, 0L)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_scrolling)
        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout).title = title

        storage = Storage(this)

        fab = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            // Preventing multiple downloads of the same file
            view.isEnabled = false
            val savedDownloadRef = storage.getLong(DOWNLOAD_REF, -1L)
            if (isDownloading(baseContext, savedDownloadRef)) return@setOnClickListener

            // Start download if user have a network connection
            if (isOnline(applicationContext)) {
                lifecycleScope.launch {
                    val downloadReference = downloadCsvDataFile(
                        this@ScrollingActivity,
                        "https://opendata.ecdc.europa.eu/covid19/casedistribution/csv",
                        "Pobieram CSV"
                    )
                    storage.putLong(DOWNLOAD_REF, downloadReference)
                    view.isEnabled = true
                }
            } else {
                view.isEnabled = true
                showLongToast(stringId = R.string.you_offline)
            }
        }

        listView = findViewById(R.id.list)
        customAdapter = CustomAdapter(this, ArrayList<DataEntity>(0))
        listView.adapter = customAdapter

        // Fill in the list view
        val file = File(getExternalFilesDir(null), "$FILE_NAME.$EXTENSION")
        if (file.exists()) {
            val arrayList = ImportCsvHelper.save(file)
            updateListView(arrayList)
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(downloadsBroadcastReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(downloadsBroadcastReceiver)
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

    private suspend fun downloadCsvDataFile(
        baseActivity: Context,
        url: String?,
        title: String?
    ): Long {
        // Check if external storage can permit read/write operations
        if (isExternalStorageWritableAndReadable()) {

            // Get last modified field for downloaded file
            val lastModified = FileDownloadHelper.getFileLastModifiedFromUrl(url)
            val lastModifiedInPrefs = storage.getLong(LAST_MODIFIED, 0L)

            // Compare last modified field of new file with previous one, to determine if an update is needed at all
            if (lastModified != lastModifiedInPrefs) {
                removeOldFile(context = baseContext) // remove previously downloaded file to save storage space

                // Save new last modified value as buffered(in case download is interrupted/canceled we haven't to store this value as LAST_MODIFIED)
                storage.putLong(LAST_MODIFIED_BUF, lastModified)

                val dm = baseActivity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                val uri = Uri.parse(url)
                val request = DownloadManager.Request(uri)

                try {
                    // Save file to the directory on the primary external storage
                    request.setDestinationInExternalFilesDir(
                        baseActivity,
                        null,
                        "$FILE_NAME.$EXTENSION"
                    )
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    request.setTitle(title)

                    // Set mime type to be able to open downloaded file from notification or downloads UI
                    val mimeType = getMimeTypeForCsv()
                    request.setMimeType(mimeType)

                    showLongToast(stringId = R.string.start_downloading)

                    return dm.enqueue(request)
                } catch (e: IllegalStateException) { // happens if the external storage directory cannot be found or created
                    showLongToast(stringId = R.string.dir_cannot_be_created)
                    return -1L
                } catch (e: IOException) { // happens if there is not enough disk space
                    showLongToast(stringId = R.string.free_up_space)
                    return -1L
                }
            } else {
                showLongToast(stringId = R.string.nothing_to_update)
                return -1L
            }
        } else {
            showLongToast(stringId = R.string.external_storage_access_warning)
            return -1L
        }
    }

    private fun updateListView(arrayList: ArrayList<DataEntity>) {
        customAdapter.arrayList?.clear()
        customAdapter.arrayList?.addAll(arrayList)
        customAdapter.notifyDataSetChanged()
    }

    private fun showLongToast(stringId: Int) {
        Toast.makeText(
            this,
            resources.getString(stringId),
            Toast.LENGTH_LONG
        ).show()
    }
}