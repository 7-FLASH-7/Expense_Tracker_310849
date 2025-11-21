package com.example.expensetracker.Repository

import com.example.expensetracker.model.Expense
import com.example.expensetracker.viewModel.composable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// here we have expense repository which can handles almost everything
// for example creating, reading deleting and updating the expense
// this will be the secure of our app
// also it will be easy as it will separate businesses and UI code
class ExpenseRepository {

    //here FirebaseAuth will get an instance and  manages database operation
    private val firestore = FirebaseFirestore.getInstance()

    // here it gets Firebase Auth to check who's logged in
    private val auth = FirebaseAuth.getInstance()

    // here it returns to the User id of whoever is logged in
    // otherwise it will throw an error if no one is logged in
    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw Exception("User not logged in")
    }

    //here it will returns a flow that will continuously streams expense the updates
    // here flow is like UI will update instantly without manual refresh
    @composable
    fun getAllExpenses(): Flow<List<Expense>> = callbackFlow {
        val userId = getCurrentUserId()

        //here it will set up a listener that watches the database
        // also query listens for changes with addSnapshotListener
        val listener = firestore.collection("expenses")
            .whereEqualTo("userId", userId)
            .orderBy("dateTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                //here if there's an error, it will close the Flow
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                // here it will convert Firestore documents to Expense objects
                val expenses = snapshot?.documents?.mapNotNull { doc ->
                    //here it will turn each document into an Expense
                    doc.toObject(Expense::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                //also it will send the updated list to whoever is listening
                trySend(expenses)
            }

        // When Flow is cancelled, it will stop listening to save battery or data
        awaitClose { listener.remove() }
    }

    // here it will fetch one specific expense from the database
    @composable
    suspend fun getExpenseById(expenseId: String): Expense? {
        return try {
            // also here it will fetch the document from Firestore
            val doc = firestore.collection("expenses")
                .document(expenseId)
                .get()
                .await()

            //here it will convert to Expense object and add the ID
            doc.toObject(Expense::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            //alos ff anything goes wrong, it will return null
            null
        }
    }

    // here it saves new expense to database
    @composable
    suspend fun addExpense(expense: Expense): Result<String> {
        return try {
            val userId = getCurrentUserId()

            // here it will Make sure this expense belongs to current user
            val expenseWithUser = expense.copy(userId = userId)

            //here it will add to Firestore for ID
            val docRef = firestore.collection("expenses")
                .add(expenseWithUser.toMap())
                .await()

            //here it will return the new document's ID
            Result.success(docRef.id) // as success it will be saved successfully
        } catch (e: Exception) {
            Result.failure(e) // as failed if there is an error
        }
    }

    // here you can update you expenses
    // it will only modify whatever is already in database
    // also firestore will ensure only user can update their own expense
    @composable
    suspend fun updateExpense(expense: Expense): Result<Unit> {
        return try {
            // here it Updates the document in Firestore
            firestore.collection("expenses")
                .document(expense.id)
                .update(expense.toMap())
                .await()

            Result.success(Unit) // as success it will be saved successfully
        } catch (e: Exception) {
            Result.failure(e) // as failed if there is an error
        }
    }

    // here you can delete an expense permanently from database
    // once it is deleted it cant be undone
    @composable
    suspend fun deleteExpense(expenseId: String): Result<Unit> {
        return try {
            //here it deletes the document from Firestore
            firestore.collection("expenses")
                .document(expenseId)
                .delete()
                .await()

            Result.success(Unit) // as success it will be saved successfully
        } catch (e: Exception) {
            Result.failure(e) // as failed , there is an error
        }
    }
    // here it will adds up all the expense amounts for the current user
    // also in dashboard it will show TotalSpent also useful for budget tracking
    @composable
    suspend fun getTotalSpent(): Double {
        return try {
            val userId = getCurrentUserId()

            // here is where it will get all expenses for this user
            val snapshot = firestore.collection("expenses")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            //here it will add up all the amounts
            snapshot.documents.sumOf { doc ->
                doc.getDouble("amount") ?: 0.0
            }
        } catch (e: Exception) {
            // If error, it will return 0
            0.0
        }
    }
}

