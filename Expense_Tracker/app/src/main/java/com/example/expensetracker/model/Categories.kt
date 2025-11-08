package com.example.expensetracker.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

annotation class Parcelize
//here it passes a category data from one to another screen
@Parcelize
//here are different types categories of expenses for user
// also display name as in user can see what expenses it is
// and for i have use colour to make it look nice and better
enum class Category(val displayName: String, val color: String) :
    Parcelable{
        FOOD("Food & Dinning","#FF6F00"), // orange
        SHOPPING("Shopping", "#E91E63"), // pink
        BILLS("Bills & Utilities","#757575"), // gray
        ENTERTAINMENT("Entertainment", "#9C27B0"), // purple
        TRANSPORT("Transportation", "#1976D2"), // blue
        EDUCATION("Education", "#3F51B5"), // indigo
        HEALTHCARE("Healthcare", "#F44336"), // Red
        TRAVEL("Travel", "#00BCD4"), // cyan
        OTHER("Other","#000000") // black


}


