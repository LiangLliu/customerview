package com.edwin.customer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.edwin.customer.draw.MyDrawView

class DrawActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw)
        lifecycle.addObserver(findViewById<MyDrawView>(R.id.myView))
    }
}