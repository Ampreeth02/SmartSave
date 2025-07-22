package com.example.personalfinancetrackerapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.personalfinancetrackerapp.model.Transaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TransactionListActivity : AppCompatActivity() {

    private lateinit var transactionRecycler: RecyclerView
    private lateinit var transactionList: MutableList<Transaction>
    private lateinit var adapter: TransactionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_list)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.selectedItemId = R.id.nav_transaction
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

                R.id.nav_setting -> {
                    startActivity(Intent(this, SettingActivity::class.java))
                    true
                }
                else -> false
            }}

        val imageView: ImageView = findViewById(R.id.imageView5)
        imageView.setOnClickListener {
            val intent = Intent(this, TransactionActivity::class.java)
            startActivity(intent)
        }


        transactionRecycler = findViewById(R.id.recyclerTransactions)
        transactionRecycler.layoutManager = LinearLayoutManager(this)

        transactionList = loadTransactions()

        adapter = TransactionAdapter(transactionList)
        transactionRecycler.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        reloadTransactions()
    }

    private fun reloadTransactions() {
        val sharedPref = getSharedPreferences("FinancePrefs", Context.MODE_PRIVATE)
        val json = sharedPref.getString("transactions", null)
        val type = object : TypeToken<MutableList<Transaction>>() {}.type

        val updatedList: MutableList<Transaction> = if (json != null) {
            Gson().fromJson(json, type)
        } else {
            mutableListOf()
        }

        transactionList.clear()
        transactionList.addAll(updatedList)
        adapter.notifyDataSetChanged()
    }

    private fun loadTransactions(): MutableList<Transaction> {
        val sharedPref = getSharedPreferences("FinancePrefs", Context.MODE_PRIVATE)
        val json = sharedPref.getString("transactions", null)
        return if (json != null) {
            val type = object : TypeToken<MutableList<Transaction>>() {}.type
            Gson().fromJson(json, type)
        } else {
            mutableListOf()
        }
    }
}
