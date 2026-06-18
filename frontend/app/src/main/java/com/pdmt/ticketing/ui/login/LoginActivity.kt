package com.pdmt.ticketing.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.pdmt.ticketing.R
import com.pdmt.ticketing.data.api.RetrofitClient
import com.pdmt.ticketing.ui.ticket.TicketListActivity
import com.pdmt.ticketing.utils.TokenManager
import com.pdmt.ticketing.viewmodel.AuthViewModel
import com.pdmt.ticketing.viewmodel.LoginState
import com.pdmt.ticketing.viewmodel.ViewModelFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var authViewModel: AuthViewModel
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Init TokenManager dan RetrofitClient
        tokenManager = TokenManager(applicationContext)
        RetrofitClient.init(tokenManager)

        // Init ViewModel
        val factory = ViewModelFactory(tokenManager)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        // Bind views
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)

        // Observasi state login
        authViewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    btnLogin.isEnabled = false
                    tvError.visibility = View.GONE
                }
                is LoginState.Success -> {
                    progressBar.visibility = View.GONE
                    btnLogin.isEnabled = true
                    // Pindah ke halaman tiket
                    val intent = Intent(this, TicketListActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                is LoginState.Error -> {
                    progressBar.visibility = View.GONE
                    btnLogin.isEnabled = true
                    tvError.text = state.message
                    tvError.visibility = View.VISIBLE
                }
                is LoginState.Idle -> {
                    progressBar.visibility = View.GONE
                    btnLogin.isEnabled = true
                }
            }
        }

        // Tombol login
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            authViewModel.login(username, password)
        }
    }
}