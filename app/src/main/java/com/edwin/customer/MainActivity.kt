package com.edwin.customer

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.edwin.customer.activity.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startActivity(Intent(this, ColorPickerActivity::class.java))

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

        findViewById<TextView>(R.id.findMe)
            .setOnClickListener {
                run {
                    startActivity(Intent(this, FindMeActivity::class.java))

                }
            }

        findViewById<TextView>(R.id.mySurfaceView)
            .setOnClickListener {
                run {
                    startActivity(Intent(this, MySurfaceViewMainActivity::class.java))

                }
            }

    }
}