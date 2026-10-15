package com.bignerdranch.android.neuimina_pr4_v5

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.StringBuilder
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences
    private val gson = Gson()
    private lateinit var backPressedCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = getSharedPreferences("ExpensesPrefs", Context.MODE_PRIVATE)

        initBackPressedLogic()

        val userJson = sharedPref.getString("user_info", null)
        if (userJson != null) {
            showMainScreen()
        } else {
            showLoginScreen()
        }
    }

    private fun initBackPressedLogic() {
        backPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                if (findViewById<Button>(R.id.btnBack) != null) {
                    showMainScreen()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    private fun showLoginScreen() {
        setContentView(R.layout.login)
        backPressedCallback.isEnabled = false

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()

            // Сравниваем длину строки напрямую
            if (username.length == 0) {
                Toast.makeText(this, "Пожалуйста, введите имя!", Toast.LENGTH_SHORT).show()
            } else {
                val userJson = gson.toJson(User(username))
                sharedPref.edit().putString("user_info", userJson).apply()
                showMainScreen()
            }
        }
    }

    private fun showMainScreen() {
        setContentView(R.layout.activity_main)
        backPressedCallback.isEnabled = false

        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val etTitle = findViewById<EditText>(R.id.etExpenseTitle)
        val etAmount = findViewById<EditText>(R.id.etExpenseAmount)
        val etCategory = findViewById<EditText>(R.id.etExpenseCategory)
        val btnSave = findViewById<Button>(R.id.btnSaveExpense)
        val btnShowList = findViewById<Button>(R.id.btnShowList)

        val userJson = sharedPref.getString("user_info", "")
        if (userJson != null && userJson != "") {
            // Передаем тип через TypeToken
            val user = gson.fromJson<User>(userJson, object : TypeToken<User>() {}.type)
            tvWelcome.text = "Добро пожаловать, " + user.username + "!"
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString()
            val amountStr = etAmount.text.toString()
            val category = etCategory.text.toString()

            if (title.length == 0 || amountStr.length == 0 || category.length == 0) {
                Toast.makeText(this, "Заполните все поля!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var amount = 0.0
            try {
                amount = java.lang.Double.parseDouble(amountStr)
            } catch (e: Exception) {
                amount = 0.0
            }

            val newExpense = Expense(title, amount, category)

            val existingExpensesJson = sharedPref.getString("expenses_list", "[]")
            val type = object : TypeToken<ArrayList<Expense>>() {}.type
            val expenseList: ArrayList<Expense> = gson.fromJson(existingExpensesJson, type) ?: ArrayList<Expense>()

            expenseList.add(newExpense)
            sharedPref.edit().putString("expenses_list", gson.toJson(expenseList)).apply()

            Toast.makeText(this, "Расход сохранен!", Toast.LENGTH_SHORT).show()

            etTitle.text.clear()
            etAmount.text.clear()
            etCategory.text.clear()
        }

        btnShowList.setOnClickListener {
            showListScreen()
        }
    }

    private fun showListScreen() {
        setContentView(R.layout.list)
        backPressedCallback.isEnabled = true

        val tvExpensesDisplay = findViewById<TextView>(R.id.tvExpensesDisplay)
        val btnBack = findViewById<Button>(R.id.btnBack)

        val expensesJson = sharedPref.getString("expenses_list", "[]")
        val type = object : TypeToken<ArrayList<Expense>>() {}.type
        val expenseList: ArrayList<Expense> = gson.fromJson(expensesJson, type) ?: ArrayList<Expense>()

        if (expenseList.size > 0) {
            val builder = StringBuilder()
            var totalSum = 0.0
            var index = 1

            for (i in 0..expenseList.size - 1) {
                val expense = expenseList[i]
                builder.append(index.toString()).append(". ").append(expense.title).append("\n")
                builder.append("   Сумма: ").append(expense.amount).append(" руб.\n")
                builder.append("   Категория: ").append(expense.category).append("\n\n")
                totalSum += expense.amount
                index++
            }
            builder.append("-----------------------------\n")
            builder.append("Итого расходов: ").append(totalSum).append(" руб.")
            tvExpensesDisplay.text = builder.toString()
        } else {
            tvExpensesDisplay.text = "Список пуст."
        }

        btnBack.setOnClickListener {
            showMainScreen()
        }
    }
}