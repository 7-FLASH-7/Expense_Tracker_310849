package com.example.expensetracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.example.expensetracker.viewModel.ExpenseViewModel
import com.example.expensetracker.viewmodel.AuthViewModel
import com.example.expensetracker.viewmodel.AuthState
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

// here this is the tag name for our log messages so we can find them easily in logcat
private const val TAG = "MainActivity"

// here this is the main activity where our app starts
// this is the first thing that runs when you open the app
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // here when app opens, first thing we do is check if Firebase is working
        testFirebaseConnection()

        // here we set up what the user will see on screen
        setContent {
            ExpenseTrackerTheme {
                // here this makes the background cover the whole screen
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // here this shows the main screen of our app
                    MainScreen()
                }
            }
        }
    }

    // here this function checks if we can talk to Firebase database
    // this is like making sure your wifi is working before watching videos
    private fun testFirebaseConnection() {
        val db = Firebase.firestore

        // here we create a small test message to send to Firebase
        val testData = hashMapOf(
            "test" to "Firebase connection successful!",
            "timestamp" to System.currentTimeMillis()
        )

        // here we try to save this test message to Firebase
        db.collection("test")
            .add(testData)
            .addOnSuccessListener { documentReference ->
                // here if it worked, we print a success message with checkmark
                Log.d(TAG, "Firebase connected! Document ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                // here if it failed, we print an error message with X mark
                Log.e(TAG, "Firebase connection failed", e)
            }
    }
}

// here this is the main screen that decides what to show the user
// if you are logged in, it shows your expenses
// if you are not logged in, it shows the login page
@Composable
fun MainScreen() {
    // here we create our ViewModels which hold all the app data
    val authViewModel: AuthViewModel = viewModel()
    val expenseViewModel: ExpenseViewModel = viewModel()

    // here we watch who is currently logged in
    // this updates automatically when someone logs in or out
    val currentUser by authViewModel.currentUser.observeAsState()
    val authState by authViewModel.authState.observeAsState(AuthState.Idle)

    // here we watch the expense data in real time
    // when you add a new expense, these numbers update automatically
    val totalSpent by expenseViewModel.totalSpent.observeAsState(0.0)
    val expenses by expenseViewModel.expenses.observeAsState(emptyList())

    // here we decide what screen to show based on if user is logged in
    when {
        currentUser != null -> {
            // here if someone is logged in, show them the main app with their expenses
            MainAppScreen(
                userName = currentUser?.displayName ?: "User",
                totalSpent = totalSpent,
                expenseCount = expenses.size,
                onLogout = { authViewModel.signOut() }
            )
        }
        else -> {
            // here if no one is logged in, show the login screen
            LoginScreen(
                authViewModel = authViewModel,
                authState = authState
            )
        }
    }
}

// here this is the main app screen that shows after you login
// it displays your name, how much you spent, and how many expenses you have
@Composable
fun MainAppScreen(
    userName: String,
    totalSpent: Double,
    expenseCount: Int,
    onLogout: () -> Unit
) {
    // here we arrange everything vertically in the center of the screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // here we show a welcome message with the user's name
        Text(
            text = "Welcome, $userName!",
            style = MaterialTheme.typography.headlineMedium
        )

        // here we add some space between elements
        Spacer(modifier = Modifier.height(24.dp))

        // here we create a nice card to show the expense summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // here we show the label for total spent
                Text(
                    text = "Total Spent",
                    style = MaterialTheme.typography.titleMedium
                )
                // here we show the actual amount in euros with 2 decimal places
                Text(
                    text = "€${String.format("%.2f", totalSpent)}",
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                // here we show how many expenses are being tracked
                Text(
                    text = "$expenseCount expenses tracked",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // here we add a sign out button so user can logout
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Out")
        }
    }
}

// here this is the login screen where users can sign in or create new account
// it has fields for email, password, and display name
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    authState: AuthState
) {
    // here we create variables to remember what user types in the fields
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }

    // here we arrange everything vertically in the center
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // here we show different title based on if user is signing up or signing in
        Text(
            text = if (isSignUp) "Create Account" else "Sign In",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // here we show display name field only when creating new account
        if (isSignUp) {
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("Display Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // here we add email input field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // here we add password input field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // here we add the main button that either signs in or creates account
        Button(
            onClick = {
                if (isSignUp) {
                    // here if in signup mode, create new account
                    authViewModel.signUp(email, password, displayName)
                } else {
                    // here if in signin mode, login to existing account
                    authViewModel.signIn(email, password)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = authState !is AuthState.Loading
        ) {
            // here we show loading spinner when processing, otherwise show button text
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(if (isSignUp) "Sign Up" else "Sign In")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // here we add a button to toggle between sign in and sign up modes
        TextButton(
            onClick = { isSignUp = !isSignUp }
        ) {
            Text(
                if (isSignUp)
                    "Already have an account? Sign In"
                else
                    "Don't have an account? Sign Up"
            )
        }

        // here we show error or success messages based on what happened
        when (authState) {
            is AuthState.Error -> {
                // here if something went wrong, show error in red
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = authState.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
            is AuthState.Success -> {
                // here if everything worked, show success message in primary color
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = authState.message,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            else -> {}
        }
    }
}

