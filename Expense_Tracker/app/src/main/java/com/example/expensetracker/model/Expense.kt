package com.example.expensetracker.model

import android.os.Parcelable
import com.example.expensetracker.viewModel.composable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date
import kotlinx.parcelize.Parcelize

// here it will show the entry of users expense
// for eg how much, on what, where (GPS) , which category etc you spend on
@Parcelize
data class Expense(
    @DocumentId
    val id:String = "", // here firestore will be auto generating this
    val userId:String = "", //here it will show who Owns this expense
    val amount: Double = 0.0, // here it will show how much was spend
    val currency:String = "EUR", // here it will show the currency you used
    val category:String = Category.OTHER.name, // here it will show what type of expense you did
    val description:String = "", // here it will be users notes
    val location:String = "", // here it will be the address
    val latitude:Double? = null, // GPS will be used here
    val longitude:Double? = null, // GPS will be used here

    @ServerTimestamp
    val dateTime:Date? = null, // here it will show when did the expense happen
    val receiptURL:String? = null, // // here it will be the receipt image
    val createdAt:Long = System.currentTimeMillis() // here it will show when the record was created

): Parcelable{

    constructor():this("","",0.0,"EUR",Category.OTHER.name,"","",null,null,null,null,0L)

    // here it will convert expense to map for firestore
@composable
    fun toMap():Map<String,Any?>{
        return mapOf(
            "id" to id,
            "userId" to userId,
            "amount" to amount,
            "currency" to currency,
            "category" to category,
            "description" to description,
            "location" to location,
            "latitude" to latitude,
            "longitude" to longitude,
            "dateTime" to (dateTime ?: Date()), // if its null it will use current date and time
            "receiptURL" to receiptURL,
            "createdAt" to createdAt,

        )
    }
// here try and catch will handel if the category text is valid or invalid or corrupted or not
    fun getCategoryEnum(): Category{
        return try {
            Category.valueOf(category)
        }catch (e: Exception){
            Category.OTHER
        }
    }
}

