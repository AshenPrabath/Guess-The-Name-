package com.example.guessthename.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.guessthename.MainActivity
import com.example.guessthename.R
import com.example.guessthename.api.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RandomNameActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var currentUserName: String
    private lateinit var wordDisplayTextView: TextView
    private lateinit var welcomeTextView: TextView
    private lateinit var generateNewWordButton: Button
    private lateinit var inputGuess: EditText
    private lateinit var verifyButton: Button
    private lateinit var inputLetter: EditText
    private lateinit var verifyLetterButton: Button
    private lateinit var checkLengthButton: Button
    private lateinit var signOutButton: Button
    private lateinit var timeDisplayTextView: TextView
    private lateinit var scoreTextView: TextView
    private var secretWord: String? = null
    private var score: Int = 100
    private var startTime: Long = 0
    private val incorrectGuessPenalty: Int = 10
    private val letterCheckPenalty: Int = 5
    private val lengthCheckPenalty: Int = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_random_name)

        initializeUIComponents()

        // Correctly retrieve the username
        currentUserName = prefs.getString("storedUserName", "Guest") ?: "Guest"
        welcomeTextView.text = "Hello, $currentUserName!"

        generateNewWord()

        generateNewWordButton.setOnClickListener { generateNewWord() }
        verifyButton.setOnClickListener { evaluateGuess() }
        verifyLetterButton.setOnClickListener { validateLetter() }
        checkLengthButton.setOnClickListener { displayWordLength() }
        signOutButton.setOnClickListener { logout() }
    }

    private fun initializeUIComponents() {
        prefs = getSharedPreferences("preferencesKey", Context.MODE_PRIVATE)
        wordDisplayTextView = findViewById(R.id.wordDisplayTextView)
        welcomeTextView = findViewById(R.id.welcomeTextView)
        generateNewWordButton = findViewById(R.id.generateNewWordButton)
        inputGuess = findViewById(R.id.inputGuess)
        verifyButton = findViewById(R.id.verifyButton)
        signOutButton = findViewById(R.id.signOutButton)
        scoreTextView = findViewById(R.id.scoreTextView)
        verifyLetterButton = findViewById(R.id.verifyLetterButton)
        inputLetter = findViewById(R.id.inputLetter)
        checkLengthButton = findViewById(R.id.checkLengthButton)
        timeDisplayTextView = findViewById(R.id.timeDisplayTextView)
    }

    private fun generateNewWord() {
        CoroutineScope(Dispatchers.IO).launch {
            secretWord = ApiClient.getRandomWord()
            withContext(Dispatchers.Main) {
                if (secretWord != null) {
                    wordDisplayTextView.text = "Hello, $currentUserName! Your word is: $secretWord"
                    resetGame()
                } else {
                    wordDisplayTextView.text = "Failed to retrieve a word."
                }
            }
        }
    }

    private fun resetGame() {
        inputGuess.text.clear()
        score = 100
        startTime = System.currentTimeMillis()
        timeDisplayTextView.text = "Timer started!"
        scoreTextView.text = "Score: $score"
        enableGameControls(true)
    }

    private fun enableGameControls(enable: Boolean) {
        inputGuess.isEnabled = enable
        verifyButton.isEnabled = enable
        verifyLetterButton.isEnabled = enable
        checkLengthButton.isEnabled = enable
    }

    private fun evaluateGuess() {
        val userGuess = inputGuess.text.toString().trim()
        if (userGuess.equals(secretWord, ignoreCase = true)) {
            val elapsedTime = (System.currentTimeMillis() - startTime) / 1000
            wordDisplayTextView.text = "Correct! The word was: $secretWord"
            timeDisplayTextView.text = "Time taken: $elapsedTime seconds"
            scoreTextView.text = "Final score: $score"
            enableGameControls(false)
        } else {
            score -= incorrectGuessPenalty
            scoreTextView.text = "Score: $score"
            if (score <= 0) {
                wordDisplayTextView.text = "Game over! The word was: $secretWord"
                enableGameControls(false)
            } else {
                wordDisplayTextView.text = "Incorrect, try again!"
            }
        }
    }

    private fun validateLetter() {
        val letter = inputLetter.text.toString().trim().lowercase().getOrNull(0)
        if (letter != null && score >= letterCheckPenalty) {
            val occurrences = secretWord?.count { it.lowercaseChar() == letter } ?: 0
            score -= letterCheckPenalty
            scoreTextView.text = "Score: $score"

            if (occurrences > 0) {
                wordDisplayTextView.text = "Letter '$letter' appears $occurrences time(s)."
            } else {
                wordDisplayTextView.text = "Letter '$letter' is not in the word."
            }

            if (score <= 0) {
                wordDisplayTextView.text = "Game over! The word was: $secretWord"
                enableGameControls(false)
            }
        } else {
            wordDisplayTextView.text = "Insufficient score to check a letter!"
        }
    }

    private fun displayWordLength() {
        if (score >= lengthCheckPenalty) {
            val length = secretWord?.length ?: 0
            score -= lengthCheckPenalty
            scoreTextView.text = "Score: $score"

            wordDisplayTextView.text = "The word has $length letters."

            if (score <= 0) {
                wordDisplayTextView.text = "Game over! The word was: $secretWord"
                enableGameControls(false)
            }
        } else {
            wordDisplayTextView.text = "Not enough score to check the length!"
        }
    }
    private fun logout() {
        // Clear the stored username from SharedPreferences
        val editor = prefs.edit()
        editor.clear()
        editor.apply()

        // Redirect back to MainActivity
        val intent = Intent(this@RandomNameActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish() // Close the current activity so the user can't go back to it
    }
}