package com.example.personalfinancetrackerapp

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.style.StyleSpan
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar

class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        val textView = findViewById<TextView>(R.id.textView6)




        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val greeting = when (currentHour) {
            in 0..11 -> "Good Morning 🌞"
            in 12..17 -> "Good Afternoon ☀️"
            else -> "Good Evening 🌙"
        }


        val sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val username = sharedPreferences.getString("currentUser", "Guest") ?: "Guest"

        val fullGreeting = "$greeting, $username"
        val styledGreeting = SpannableString(fullGreeting)
        styledGreeting.setSpan(
            StyleSpan(Typeface.BOLD),
            greeting.length + 2,
            fullGreeting.length,
            0
        )
        textView.text = styledGreeting

        // Navigation to Reminder Settings
        val liner2 = findViewById<LinearLayout>(R.id.liner1)
        liner2.setOnClickListener {
            val intent = Intent(this, ReminderSettingsActivity::class.java)
            startActivity(intent)
        }

        // Navigation to Category Chart
        val liner = findViewById<LinearLayout>(R.id.liner2)
        liner.setOnClickListener {
            val intent = Intent(this, CategoryChartActivity::class.java)
            startActivity(intent)
        }

        // Log out (go to login activity)
        val liner4 = findViewById<LinearLayout>(R.id.liner4)
        liner4.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Bottom Navigation setup
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_setting
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_budget -> {
                    startActivity(Intent(this, BudgetActivity::class.java))
                    true
                }

                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }

                R.id.nav_transaction -> {
                    startActivity(Intent(this, TransactionListActivity::class.java))
                    true
                }

                else -> false
            }
        }
    }
}
