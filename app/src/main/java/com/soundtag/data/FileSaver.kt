package com.soundtag.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
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
            val audioUri = if (Build.VERSION.SDK_INT >= 29) {
                saveAudioMediaStore(context, audioFile, desiredName)
            } else {
                saveAudioLegacy(audioFile, desiredName)
            }

            if (Build.VERSION.SDK_INT >= 29) {
                saveJsonMediaStore(context, desiredName, jsonContent)
            } else {
                saveJsonLegacy(desiredName, jsonContent)
            }

            audioUri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // API 29+ : MediaStore
    private fun saveAudioMediaStore(context: Context, sourceFile: File, name: String): Uri? {
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
            sourceFile.inputStream().use { input -> input.copyTo(output) }
        }

        values.clear()
        values.put(MediaStore.Audio.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)

        sourceFile.delete()
        return uri
    }

    private fun saveJsonMediaStore(context: Context, name: String, jsonContent: String) {
        val values = ContentValues().apply {
            put(MediaStore.Files.FileColumns.DISPLAY_NAME, "$name.json")
            put(MediaStore.Files.FileColumns.MIME_TYPE, "application/json")
            put(MediaStore.Files.FileColumns.RELATIVE_PATH, "Documents/SoundTag")
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

    // API < 29 : Legacy file I/O
    @Suppress("DEPRECATION")
    private fun saveAudioLegacy(sourceFile: File, name: String): Uri {
        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "SoundTag")
        dir.mkdirs()
        val dest = File(dir, "$name.m4a")
        sourceFile.copyTo(dest, overwrite = true)
        sourceFile.delete()
        return Uri.fromFile(dest)
    }

    @Suppress("DEPRECATION")
    private fun saveJsonLegacy(name: String, jsonContent: String) {
        val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SoundTag")
        dir.mkdirs()
        val dest = File(dir, "$name.json")
        dest.writeText(jsonContent, Charsets.UTF_8)
    }
}
