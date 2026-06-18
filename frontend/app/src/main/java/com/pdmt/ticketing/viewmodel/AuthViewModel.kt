package com.pdmt.ticketing.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdmt.ticketing.data.model.User
import com.pdmt.ticketing.data.repository.TicketRepository
import com.pdmt.ticketing.utils.TokenManager
import kotlinx.coroutines.launch

class AuthViewModel(private val tokenManager: TokenManager) : ViewModel() {

    private val repository = TicketRepository()

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Username dan password wajib diisi")
            return
        }

        _loginState.value = LoginState.Loading

        viewModelScope.launch {
            val result = repository.login(username, password)
            result.fold(
                onSuccess = { response ->
                    // Simpan token dan info user
                    tokenManager.saveToken(response.token)
                    tokenManager.saveUserInfo(
                        response.user.username,
                        response.user.role,
                        response.user.name
                    )
                    _currentUser.value = response.user
                    _loginState.value = LoginState.Success(response.user.role)
                },
                onFailure = { error ->
                    _loginState.value = LoginState.Error(error.message ?: "Login gagal")
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearAll()
            _currentUser.value = null
            _loginState.value = LoginState.Idle
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val role: String) : LoginState()
    data class Error(val message: String) : LoginState()
}