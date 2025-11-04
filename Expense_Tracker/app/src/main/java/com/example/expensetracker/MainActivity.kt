package com.example.expensetracker

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.expensetracker.ui.theme.ExpenseTrackerTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

private const val TAG = "FirestoreDebug"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ExpenseTrackerTheme {
                Screen()
            }
        }
    }
}

@Composable
fun Screen() {

    val db = Firebase.firestore

    // ✅ Run Firestore write only once when composable first loads
    LaunchedEffect(Unit) {
        val user = hashMapOf(
            "first" to "sdsdsd",
            "last" to "Lovelace",
            "born" to 1815,
        )

        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "✅ Document added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "❌ Error adding document", e)
            }
    }

    Text("Firestore Write Test ✅")
}
