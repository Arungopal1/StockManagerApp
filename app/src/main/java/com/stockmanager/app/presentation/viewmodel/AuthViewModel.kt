package com.stockmanager.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stockmanager.app.data.entity.User
import com.stockmanager.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val currentUser = _currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isLoading = _isLoading
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val errorMessage = _errorMessage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isLoggedIn: Boolean
        get() = _currentUser.value != null

    suspend fun login(username: String, password: String): Boolean {
        _isLoading.value = true
        _errorMessage.value = null

        // Hash the password (in real app, use proper hashing like bcrypt)
        val passwordHash = hashPassword(password)

        val user = userRepository.authenticate(username, passwordHash)

        _isLoading.value = false

        return if (user != null) {
            _currentUser.value = user
            userRepository.updateLastLogin(user.id)
            true
        } else {
            _errorMessage.value = "Invalid username or password"
            false
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // Permission checking
    fun hasPermission(permission: com.stockmanager.app.data.entity.UserPermission): Boolean {
        val user = _currentUser.value
        return user != null && userRepository.hasPermission(user, permission)
    }

    fun requirePermission(permission: com.stockmanager.app.data.entity.UserPermission): Boolean {
        return hasPermission(permission)
    }

    private fun hashPassword(password: String): String {
        // TODO: Use proper password hashing (bcrypt, PBKDF2, etc.)
        // For now, simple hash - REPLACE IN PRODUCTION
        return password.hashCode().toString()
    }
}