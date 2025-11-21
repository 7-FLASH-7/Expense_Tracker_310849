package com.example.expensetracker.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.repository.AuthenticationRepo
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch


// this is the controller for login and signup screens
// this ome sits perfectly between UI and data and also your data wont get lost
// here it handles, manages and tracks the login and sign up
class AuthViewModel : ViewModel() {

    // this is our data source for authentication
    private val repository = AuthenticationRepo()

    // here it will track what exactly its happening
    // for example loading, success of login and sign up
    // also keeps for error or if user sign out
    private val _authState = MutableLiveData<AuthState>() // here mutable data can change the value
    val authState: LiveData<AuthState> = _authState // this is for read only where activity can be observe

    // here it will holds the information about who log in or it will be null if its no one
    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    //here it will run when as viewmodel is first created
    // also checks if someone is already logged in
    init {
        _currentUser.value = repository.getCurrentUser()
    }

    // this is the method which is called when user clicks on create account button
    fun signUp(email: String, password: String, displayName: String) {
        //here it will run in background and will not let UI freeze
        viewModelScope.launch { // also it will automatically canceled if viewmodel is destroyed
            // here it will show you loading spinner in UI
            _authState.value = AuthState.Loading

            // here it will attempt to create an account
            val result = repository.signUp(email, password, displayName)

            if (result.isSuccess) {
                //if it is Success, then it will Update the current user
                _currentUser.value = result.getOrNull()
                _authState.value = AuthState.Success("Account created successfully!")
            } else {
                // if it Failed  it will show an error message
                _authState.value = AuthState.Error(
                    result.exceptionOrNull()?.message ?: "Sign up failed"
                )
            }
        }
    }

    // this is for existing user
    // this method is called when user clicks on login button
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading

            val result = repository.signIn(email, password)

            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
                // if it succeed
                _authState.value = AuthState.Success("Logged in successfully!")
            } else {
                // if it failed
                _authState.value = AuthState.Error(
                    result.exceptionOrNull()?.message ?: "Login failed"
                )
            }
        }
    }

    // this method is called when users clicks on sign out
    // which will clears the current user and updates the state
    fun signOut() {
        repository.signOut()
        _currentUser.value = null
        _authState.value = AuthState.SignedOut
    }

    // this id when it checks if someone is logged in right now
    fun isUserLoggedIn(): Boolean {
        return repository.isUserLoggedIn()
    }
}

// this authentication sealed class states and represents for all login and sign up processes

sealed class AuthState { // this sealed class is for when only 5 states can represent

    object Idle : AuthState() // here where nothing is happening, waiting for user action
    object Loading : AuthState() // here where network is request in progress
    data class Success(val message: String) : AuthState() // here where operation is completed successfully
    data class Error(val message: String) : AuthState() // here where something went wrong
    object SignedOut : AuthState() // this is just that user just logged out
}

