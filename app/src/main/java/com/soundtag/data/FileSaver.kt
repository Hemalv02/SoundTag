package com.soundtag.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object FileSaver {

    suspend fun saveRecording(
        context: Context,
        audioFile: File,
        desiredName: String,
        jsonContent: String
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val audioUri = saveAudioFile(context, audioFile, desiredName)
            saveJsonFile(context, desiredName, jsonContent)
            audioUri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveAudioFile(context: Context, sourceFile: File, name: String): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, "$name.m4a")
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4")
            put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/SoundTag")
            put(MediaStore.Audio.Media.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
            ?: return null

        resolver.openOutputStream(uri)?.use { output ->
            sourceFile.inputStream().use { input ->
                input.copyTo(output)
            }
        }

        values.clear()
        values.put(MediaStore.Audio.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)

        // Clean up temp file
        sourceFile.delete()

        return uri
    }

    private fun saveJsonFile(context: Context, name: String, jsonContent: String) {
        val values = ContentValues().apply {
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, "$name.json")
            put(MediaStore.Files.FileColumns.MIME_TYPE, "application/json")
            put(MediaStore.Files.FileColumns.RELATIVE_PATH, "Music/SoundTag")
            put(MediaStore.Files.FileColumns.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), values)
            ?: return

        resolver.openOutputStream(uri)?.use { output ->
            output.write(jsonContent.toByteArray(Charsets.UTF_8))
        }

        values.clear()
        values.put(MediaStore.Files.FileColumns.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
    }
}
