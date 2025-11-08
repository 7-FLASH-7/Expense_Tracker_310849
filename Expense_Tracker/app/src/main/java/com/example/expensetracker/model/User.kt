package com.example.expensetracker.model

import com.example.expensetracker.viewModel.composable

//Here it will represent that an user is using an app
// also the data will  be stored in firestore database

data class User (
    val uniqueID:String = "", // this will be uniques id from firebase
    val email:String = "", // users email
    val displayName:String = "", // users name
    val photoURL:String = "", // profile picture if user wants to  keep it
    val currency:String = "EUR", // our default currency will be EURO
    val createdAt:Long = System.currentTimeMillis() // this will be current timestamp when an account will be created
    )

// it is an empty constructor because the firebase firestore will convert data from database into user object
{ constructor(): this ("","","","","EUR",0L)

    @composable
    // this will convert user to map
    fun toMap(): Map<String,Any>{
        return mapOf(
            "uniqueID" to uniqueID,
            "email" to email,
            "displayName" to displayName,
            "photoURL" to photoURL,
            "currency" to currency,
            "createdAt" to createdAt
        )
    }

}