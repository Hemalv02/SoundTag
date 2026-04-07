package com.soundtag.data

import android.content.Context
import java.io.File

data class PendingUpload(
    val filename: String,
    val audioFile: File,
    val jsonFile: File,
    val annotatorId: String
)

class UploadQueueManager(private val context: Context) {

    private val queueDir: File
        get() = File(context.filesDir, "pending_uploads").also { it.mkdirs() }

    fun queueForUpload(audioFile: File, jsonContent: String, filename: String, annotatorId: String) {
        val destAudio = File(queueDir, "$filename.m4a")
        val destJson = File(queueDir, "$filename.json")
        val destMeta = File(queueDir, "$filename.meta")

        audioFile.copyTo(destAudio, overwrite = true)
        destJson.writeText(jsonContent, Charsets.UTF_8)
        destMeta.writeText(annotatorId, Charsets.UTF_8)
    }

    fun getPendingFiles(): List<PendingUpload> {
        val dir = queueDir
        if (!dir.exists()) return emptyList()

        return dir.listFiles { f -> f.extension == "m4a" }
            ?.mapNotNull { audioFile ->
                val base = audioFile.nameWithoutExtension
                val jsonFile = File(dir, "$base.json")
                val metaFile = File(dir, "$base.meta")
                if (jsonFile.exists() && metaFile.exists()) {
                    PendingUpload(
                        filename = base,
                        audioFile = audioFile,
                        jsonFile = jsonFile,
                        annotatorId = metaFile.readText().trim()
                    )
                } else null
            } ?: emptyList()
    }

    fun removePending(filename: String) {
        File(queueDir, "$filename.m4a").delete()
        File(queueDir, "$filename.json").delete()
        File(queueDir, "$filename.meta").delete()
    }

    fun hasPending(): Boolean = getPendingFiles().isNotEmpty()
}
