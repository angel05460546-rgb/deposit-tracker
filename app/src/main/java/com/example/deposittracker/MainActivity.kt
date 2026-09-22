package com.example.deposittracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var totalText: TextView
    private lateinit var permissionButton: Button
    private lateinit var historyContainer: LinearLayout
    private lateinit var amountInput: EditText

    private val smsPermissions = arrayOf(
        Manifest.permission.RECEIVE_SMS,
        Manifest.permission.READ_SMS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun buildUi() {
        val pad = dp(20)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, dp(40), pad, pad)
        }

        root.addView(TextView(this).apply {
            text = "دفترچه واریز روزانه"
            textSize = 20f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })

        permissionButton = Button(this).apply {
            text = "اجازه‌ی خواندن پیامک رو فعال کن"
            setOnClickListener {
                ActivityCompat.requestPermissions(this@MainActivity, smsPermissions, 100)
            }
        }
        root.addView(permissionButton, params(dp(16)))

        totalText = TextView(this).apply {
            textSize = 28f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        root.addView(totalText, params(dp(24)))

        root.addView(TextView(this).apply {
            text = "مجموع واریزی (تومان)"
            textSize = 13f
            gravity = Gravity.CENTER
            alpha = 0.6f
        }, params(dp(2)))

        root.addView(TextView(this).apply {
            text = "ثبت واریز دستی"
            textSize = 15f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }, params(dp(28)))

        amountInput = EditText(this).apply {
            hint = "مبلغ به تومان"
            inputType = InputType.TYPE_CLASS_NUMBER
        }
        root.addView(amountInput, params(dp(8)))

        root.addView(Button(this).apply {
            text = "افزودن"
            setOnClickListener {
                val amount = amountInput.text.toString().toLongOrNull()
                if (amount != null && amount > 0) {
                    Store.addEntry(this@MainActivity, amount, "دستی")
                    amountInput.setText("")
                    refresh()
                } else {
                    Toast.makeText(this@MainActivity, "یه مبلغ معتبر وارد کن", Toast.LENGTH_SHORT).show()
                }
            }
        }, params(dp(8)))

        root.addView(TextView(this).apply {
            text = "تاریخچه"
            textSize = 15f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }, params(dp(28)))

        historyContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(historyContainer, params(dp(8)))

        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)
    }

    private fun refresh() {
        val hasPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
        permissionButton.visibility = if (hasPermission) View.GONE else View.VISIBLE

        val entries = Store.getEntries(this)
        val total = entries.sumOf { it.amount }
        totalText.text = String.format("%,d", total)

        historyContainer.removeAllViews()
        if (entries.isEmpty()) {
            historyContainer.addView(TextView(this).apply {
                text = "هنوز واریزی ثبت نشده"
                alpha = 0.6f
            })
        } else {
            entries.sortedByDescending { it.date }.forEach { entry ->
                historyContainer.addView(TextView(this).apply {
                    text = "${entry.date}   —   ${String.format("%,d", entry.amount)} تومان   (${entry.source})"
                    setPadding(0, dp(6), 0, dp(6))
                })
            }
        }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun params(top: Int): LinearLayout.LayoutParams {
        val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        p.topMargin = top
        return p
    }
}
