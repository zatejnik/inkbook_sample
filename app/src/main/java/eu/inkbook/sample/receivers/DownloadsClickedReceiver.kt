package eu.inkbook.sample.receivers

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import eu.inkbook.sample.helpers.FileDownloadHelper
import eu.inkbook.sample.helpers.FileDownloadHelper.EXTENSION
import eu.inkbook.sample.helpers.FileDownloadHelper.FILE_NAME
import java.io.File


class DownloadsClickedReceiver : BroadcastReceiver() {
    val TAG = DownloadsClickedReceiver::class.java.simpleName

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            DownloadManager.ACTION_NOTIFICATION_CLICKED -> {
                val file = File(context?.getExternalFilesDir(null), "$FILE_NAME.$EXTENSION")
                var fileContent: String? = ""
                if (context != null) {
                    fileContent = FileDownloadHelper.getFileContentOrNull(file.absolutePath)
                }
                Log.e(TAG, "File content: $fileContent")

                val pm = context?.packageManager
                val launchIntent = pm?.getLaunchIntentForPackage(context.packageName)
                context?.startActivity(launchIntent)
            }
        }
    }
}