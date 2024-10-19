package com.example.guessthename

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.guessthename.ui.RandomNameActivity

class MainActivity : AppCompatActivity() {

    private lateinit var inputField: EditText
    private lateinit var nextButton: Button
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("preferencesKey", Context.MODE_PRIVATE)

        // Check if a username exists
        val storedName = prefs.getString("storedUserName", null)
        if (storedName != null) {
            // If the name is stored, skip the current screen
            val goToGenerator = Intent(this@MainActivity, RandomNameActivity::class.java)
            startActivity(goToGenerator)
            finish() // Close current activity to prevent returning
            return // Exit early to avoid loading the layout
        }
        setContentView(R.layout.activity_main)

        inputField = findViewById(R.id.nameInputEditText)
        nextButton = findViewById(R.id.nextButton)

        nextButton.setOnClickListener {
            val name = inputField.text.toString().trim()
            if (name.isNotEmpty()) {
                // Store the username in SharedPreferences
                val editor = prefs.edit()
                editor.putString("storedUserName", name)
                editor.apply()

                // Navigate to the next activity
                val nextActivity = Intent(this@MainActivity, RandomNameActivity::class.java)
                nextActivity.putExtra("user", name)
                startActivity(nextActivity)
                finish() // Close this activity so it's inaccessible via back button
            } else {
                Toast.makeText(this, "Please provide a name", Toast.LENGTH_SHORT).show()
            }
        }
    }
}