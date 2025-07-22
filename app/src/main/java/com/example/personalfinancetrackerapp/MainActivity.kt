package com.example.personalfinancetrackerapp

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.personalfinancetrackerapp.model.Transaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var tvWarningMain: TextView

    private val importJsonLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            if (uri != null) {
                importJsonFromUri(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // In onCreate method of HomeActivity
        val username = intent.getStringExtra("username") ?: "Guest"
        val welcomeTextView = findViewById<TextView>(R.id.tvWelcome)
        welcomeTextView.text = "Welcome, $username!"

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_home
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_transaction -> {
                    startActivity(Intent(this, TransactionListActivity::class.java))
                    true
                }

                R.id.nav_budget -> {
                    startActivity(Intent(this, BudgetActivity::class.java))
                    true
                }

                R.id.nav_setting -> {
                    startActivity(Intent(this, SettingActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Ensure the TextView for warnings is initialized
        tvWarningMain = findViewById(R.id.tvWarningMain)

        // Replace buttons with clickable icons (ImageViews)
        val imgAddTransaction = findViewById<ImageView>(R.id.imgAddTransaction)
        val imgViewTransactions = findViewById<ImageView>(R.id.imgViewTransactions)

        // Add Transaction icon click
        imgAddTransaction.setOnClickListener {
            startActivity(Intent(this, TransactionActivity::class.java))
        }

        // View Transactions icon click
        imgViewTransactions.setOnClickListener {
            startActivity(Intent(this, TransactionListActivity::class.java))
        }

        val btnExport = findViewById<Button>(R.id.btnExport)
        val btnImport = findViewById<Button>(R.id.btnImport)

        btnExport.setOnClickListener {
            exportTransactions()
        }

        btnImport.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            }
            importJsonLauncher.launch(intent)
        }

        createNotificationChannel()

        // Request POST_NOTIFICATIONS permission for Android 13 and later
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showBudgetWarning(tvWarningMain)
    }

    private fun showBudgetWarning(warningTextView: TextView) {
        val prefs = getSharedPreferences("FinancePrefs", Context.MODE_PRIVATE)
        val budget = prefs.getFloat("budget", 0f)

        val json = prefs.getString("transactions", null)
        val type = object : TypeToken<List<Transaction>>() {}.type
        val transactions: List<Transaction> = if (json != null) Gson().fromJson(json, type) else emptyList()

        val totalSpent = transactions.sumOf { it.amount }.toFloat()
        val percentageUsed = if (budget > 0) (totalSpent / budget) * 100 else 0f

        val warningText = when {
            budget == 0f -> "⚠️ Budget not set!"
            percentageUsed > 100 -> {
                sendBudgetNotification("❌ You’ve exceeded your budget!")
                "❌ You have exceeded your budget!"
            }
            percentageUsed > 80 -> {
                sendBudgetNotification("⚠️ You're close to exceeding your budget.")
                "⚠️ You're close to exceeding your budget!"
            }
            else -> ""
        }

        warningTextView.text = warningText
    }

    private fun sendBudgetNotification(message: String) {
        val builder = NotificationCompat.Builder(this, "budget_alerts")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("💰 Budget Alert")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        with(NotificationManagerCompat.from(this)) {
            notify(1001, builder.build())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "budget_alerts",
                "Budget Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for approaching or exceeded budgets"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun exportTransactions() {
        val prefs = getSharedPreferences("FinancePrefs", Context.MODE_PRIVATE)
        val json = prefs.getString("transactions", null)

        if (json == null) {
            Toast.makeText(this, "No transactions to export", Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = "transactions_backup.json"

        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "application/json")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        if (uri != null) {
            val outputStream: OutputStream? = contentResolver.openOutputStream(uri)
            outputStream?.use {
                it.write(json.toByteArray())
                it.flush()
                Toast.makeText(this, "✅ Exported to Downloads/$fileName", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "❌ Failed to export", Toast.LENGTH_SHORT).show()
        }
    }

    private fun importJsonFromUri(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val json = reader.readText()
            reader.close()

            val prefs = getSharedPreferences("FinancePrefs", Context.MODE_PRIVATE).edit()
            prefs.putString("transactions", json)
            prefs.apply()

            Toast.makeText(this, "✅ Transactions imported successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Failed to import JSON: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
