package com.edwin.customer

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.editTextWithClear)
            .setOnClickListener {
                run {
                    startActivity(Intent(this, EditTextWithClearActivity::class.java))
                }
            }

        findViewById<TextView>(R.id.myDrawView)
            .setOnClickListener {
                run {
                    startActivity(Intent(this, DrawActivity::class.java))
                }
            }

    }
}