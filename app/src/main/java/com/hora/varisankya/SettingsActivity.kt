package com.hora.varisankya

import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()

        setupThemeToggle()
        setupLogoutButton()
    }

    private fun setupThemeToggle() {
        val themeToggleGroup = findViewById<ChipGroup>(R.id.theme_toggle_group)
        when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_NO -> themeToggleGroup.check(R.id.theme_light)
            AppCompatDelegate.MODE_NIGHT_YES -> themeToggleGroup.check(R.id.theme_dark)
            else -> themeToggleGroup.check(R.id.theme_device)
        }

        themeToggleGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                group.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                when (checkedIds[0]) {
                    R.id.theme_light -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    R.id.theme_dark -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    R.id.theme_device -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
        }
    }

    private fun setupLogoutButton() {
        val logoutButton = findViewById<Button>(R.id.logout_button)
        logoutButton.setOnClickListener { view ->
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)

            // Sign out of Firebase
            auth.signOut()

            // Return to the main activity, which will now show the login screen
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        window.decorView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        finish()
        return true
    }
}