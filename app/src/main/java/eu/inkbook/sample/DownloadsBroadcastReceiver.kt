package eu.inkbook.sample

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.util.Log
import java.io.File

class DownloadsBroadcastReceiver(private val baseActivity: ScrollingActivity): BroadcastReceiver() {

    companion object {
        val TAG = DownloadsBroadcastReceiver::class.java.simpleName
    }

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            DownloadManager.ACTION_DOWNLOAD_COMPLETE -> {
                val downloadId =
                    intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (downloadId == -1L) return

                // query download status
                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
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

                        baseActivity.updateListView(arrayList)
                    } else {
                        // download is assumed cancelled
                    }
                } else {
                    // download is assumed cancelled
                }
            }
            DownloadManager.ACTION_NOTIFICATION_CLICKED -> {
                Log.e(TAG, "Notification clicked")
            }
        }
    }
}