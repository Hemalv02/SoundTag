package com.soundtag.data

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

sealed class UploadResult {
    data object Success : UploadResult()
    data class Failed(val message: String) : UploadResult()
}

object DriveUploader {

    private const val APP_NAME = "SoundTag"

    fun getSignInIntent(context: Context): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        return GoogleSignIn.getClient(context, gso).signInIntent
    }

    fun handleSignInResult(data: Intent?): Boolean {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        return try {
            task.getResult(Exception::class.java) != null
        } catch (_: Exception) {
            false
        }
    }

    fun isSignedIn(context: Context): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null && account.grantedScopes.contains(Scope(DriveScopes.DRIVE_FILE))
    }

    fun getSignedInEmail(context: Context): String? {
        return GoogleSignIn.getLastSignedInAccount(context)?.email
    }

    suspend fun uploadRecording(
        context: Context,
        audioFile: File,
        jsonContent: String,
        filename: String,
        annotatorId: String
    ): UploadResult = withContext(Dispatchers.IO) {
        try {
            val driveService = buildDriveService(context)
                ?: return@withContext UploadResult.Failed("Not signed in")

            val folderId = findOrCreateFolder(driveService, annotatorId)

            // Upload audio
            val audioMetadata = com.google.api.services.drive.model.File().apply {
                name = "$filename.m4a"
                parents = listOf(folderId)
            }
            val audioContent = FileContent("audio/mp4", audioFile)
            driveService.files().create(audioMetadata, audioContent)
                .setFields("id")
                .execute()

            // Upload JSON sidecar
            val jsonFile = File.createTempFile("metadata", ".json", context.cacheDir)
            jsonFile.writeText(jsonContent)
            val jsonMetadata = com.google.api.services.drive.model.File().apply {
                name = "$filename.json"
                parents = listOf(folderId)
            }
            val jsonFileContent = FileContent("application/json", jsonFile)
            driveService.files().create(jsonMetadata, jsonFileContent)
                .setFields("id")
                .execute()
            jsonFile.delete()

            UploadResult.Success
        } catch (e: Exception) {
            UploadResult.Failed(e.message ?: "Upload failed")
        }
    }

    private fun buildDriveService(context: Context): Drive? {
        val account = GoogleSignIn.getLastSignedInAccount(context) ?: return null
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName(APP_NAME).build()
    }

    private fun findOrCreateFolder(driveService: Drive, annotatorId: String): String {
        // Find or create SoundTag root folder
        val rootFolderId = findOrCreateSingleFolder(driveService, APP_NAME, "root")
        // Find or create annotator subfolder
        return findOrCreateSingleFolder(driveService, annotatorId, rootFolderId)
    }

    private fun findOrCreateSingleFolder(driveService: Drive, name: String, parentId: String): String {
        val query = "name='$name' and mimeType='application/vnd.google-apps.folder' and '$parentId' in parents and trashed=false"
        val result = driveService.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id)")
            .execute()

        if (result.files.isNotEmpty()) {
            return result.files[0].id
        }

        val folderMetadata = com.google.api.services.drive.model.File().apply {
            this.name = name
            this.mimeType = "application/vnd.google-apps.folder"
            this.parents = listOf(parentId)
        }
        val folder = driveService.files().create(folderMetadata)
            .setFields("id")
            .execute()
        return folder.id
    }
}
