package com.soundtag.data

import android.app.Application
import android.content.Context

class UserPreferences(application: Application) {

    private val prefs = application.getSharedPreferences("soundtag_prefs", Context.MODE_PRIVATE)

    var annotatorName: String
        get() = prefs.getString("annotator_name", "") ?: ""
        set(value) = prefs.edit().putString("annotator_name", value).apply()

    var annotatorId: String
        get() = prefs.getString("annotator_id", "") ?: ""
        set(value) = prefs.edit().putString("annotator_id", value).apply()

    var isSetupComplete: Boolean
        get() = prefs.getBoolean("setup_complete", false)
        set(value) = prefs.edit().putBoolean("setup_complete", value).apply()
}
