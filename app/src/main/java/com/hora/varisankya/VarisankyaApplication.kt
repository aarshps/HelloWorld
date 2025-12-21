package com.hora.varisankya

import android.app.Application
import com.google.android.material.color.DynamicColors

class VarisankyaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Apply dynamic colors to all activities if available
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}