package com.soundtag.service

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.soundtag.data.DriveUploader
import com.soundtag.data.RecordingRepository
import com.soundtag.data.UploadQueueManager
import com.soundtag.data.UploadResult

class UploadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val queueManager = UploadQueueManager(applicationContext)
        val repo = RecordingRepository(applicationContext as android.app.Application)

        if (!DriveUploader.isSignedIn(applicationContext)) {
            Log.w("UploadWorker", "Not signed in, skipping")
            return Result.failure()
        }

        val pending = queueManager.getPendingFiles()
        Log.d("UploadWorker", "Pending uploads: ${pending.size}")
        if (pending.isEmpty()) return Result.success()

        var allSucceeded = true

        pending.forEach { upload ->
            val jsonContent = upload.jsonFile.readText(Charsets.UTF_8)
            val result = DriveUploader.uploadRecording(
                context = applicationContext,
                audioFile = upload.audioFile,
                jsonContent = jsonContent,
                filename = upload.filename,
                annotatorId = upload.annotatorId,
                customFolderId = upload.customFolderId.ifEmpty { null }
            )

            when (result) {
                is UploadResult.Success -> {
                    Log.d("UploadWorker", "Uploaded: ${upload.filename}")
                    queueManager.removePending(upload.filename)
                    repo.updateUploadStatus(upload.filename, "uploaded")
                }
                is UploadResult.Failed -> {
                    Log.e("UploadWorker", "Failed: ${upload.filename} - ${result.message}")
                    repo.updateUploadStatus(upload.filename, "failed")
                    allSucceeded = false
                }
            }
        }

        return if (allSucceeded) Result.success() else Result.retry()
    }

    companion object {
        private const val WORK_NAME = "soundtag_upload_sync"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<UploadWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
        }

        fun enqueueIfPending(context: Context) {
            val queueManager = UploadQueueManager(context)
            if (queueManager.hasPending()) {
                enqueue(context)
            }
        }
    }
}
