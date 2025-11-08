package com.example.expensetracker.repository

import com.example.expensetracker.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// here we have authentication repository which can handles almost everything
// for example users data like login, signUp and authentication
// this will be the secure of our app
// also it will be easy as it will separate businesses and UI code
class AuthenticationRepo{

    // FirebaseAuth is used for login and signup
    //here FirebaseAuth will get an instance and  manages user authentication
    private val auth = FirebaseAuth.getInstance()

    // firestore will be storing the data for users profile
    // here firestore will get an instance and manages our cloud database
    private val firestore = FirebaseFirestore.getInstance()


    // here it will return to current user if someone is logged in otherwise it will go null
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    // here it will check if someone is logged in or not
    fun isUserLoggedIn(): Boolean = auth.currentUser != null


    // here it will create a new account for user
    // here suspend will work smoothly but also it takes time to network requesting to firebase
    suspend fun signUp(email:String, password:String, displayName:String): Result<FirebaseUser> {
        return try {
            // and here you have to add your information to create an account for example name email and password
            val result = auth.createUserWithEmailAndPassword(email,password).await()

            // here it will make sure you actually got a user back
            val user = result.user ?: throw Exception("User creation is failed")

            // here it will show a welcome message after your account has been created
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()
            user.updateProfile(profileUpdates).await()

            // here ir creates users profile in our database
            createUserDocument(user, displayName)

            //here success will work if everything is alright
            Result.success(user) // return to user
        } catch(e: Exception){
            // here failure will show as something error has been appeared
            Result.failure(e)
        }
    }

    // here it will login with the user
    // firebase will check if email exits and if the password is correct
    // also will checked if account is enabled
    suspend fun signIn(email:String, password:String): Result<FirebaseUser> {
        return try{

            //here it will attempt to sign in with provided credentials
            val result = auth.signInWithEmailAndPassword(email,password).await()

            // here it will make sure you actually got a user back
            val user = result.user ?: throw Exception("Sign in Failed")

            //here success will work if everything is alright
            Result.success(user)
        } catch(e: Exception){
            // here failure will show as worng password or account doesnt exists
            Result.failure(e)
        }
    }

    // here it will logs out the current user
    fun signOut(){
        auth.signOut()
    }

    // here user can reset his password
    // here it will send you password resent email to user
    // after clicking on link you can reset your new password
    suspend fun resetPassword(email: String): Result<Unit>{
        return try {

            // here firebase will send the reset email automatically
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    // here it will create user document in firestore
    // so after creating an auth account we also create a profile document in our database
    // which stores extra information like currency preferences
    // private function will only used when internally signing up
    private suspend fun createUserDocument(firebaseUser:FirebaseUser, displayName:String){
        //here it will create a User object with the new user's info
        val user = User(
            uniqueID = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = displayName,
            photoURL = firebaseUser.photoUrl?.toString() ?: ""
        )

        // here it save users information to firestore
        firestore.collection("users")
            .document(firebaseUser.uid)
            .set(user.toMap())
            .await()
    }
}