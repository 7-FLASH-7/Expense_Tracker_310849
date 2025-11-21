package com.example.expensetracker.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// This annotation lets us pass category data between different screens in our app
// Think of it like putting data in an envelope to send it somewhere
@Parcelize
enum class Category(val displayName: String, val color: String) : Parcelable {

    // Here we define 9 different expense categories
    // Each category has a friendly name and a color to make the app look nice
    FOOD("Food & Dinning","#FF6F00"),        // Orange
    SHOPPING("Shopping", "#E91E63"),          // Pink
    BILLS("Bills & Utilities","#757575"),     // Gray
    ENTERTAINMENT("Entertainment", "#9C27B0"), // Purple
    TRANSPORT("Transportation", "#1976D2"),    // Blue
    EDUCATION("Education", "#3F51B5"),         // Indigo
    HEALTHCARE("Healthcare", "#F44336"),       // Red
    TRAVEL("Travel", "#00BCD4"),               // Cyan
    OTHER("Other","#000000");                  // Black

    companion object {
        // here it reads what you type and automatically guesses which category it belongs to
        // For example: if you type "pizza" it knows that's FOOD
        fun autoDetect(description: String): Category {

            // First, we make everything lowercase so "PIZZA" and "pizza" are treated the same
            val lowerDesc = description.lowercase()

            // Now we check what words are in the description and pick the right category
            return when {

                // this is for FOOD!
                lowerDesc.contains("food") || lowerDesc.contains("restaurant") ||
                        lowerDesc.contains("lunch") || lowerDesc.contains("dinner") ||
                        lowerDesc.contains("breakfast") || lowerDesc.contains("cafe") ||
                        lowerDesc.contains("pizza") || lowerDesc.contains("burger") -> FOOD

                // this is for SHOPPING!
                lowerDesc.contains("shop") || lowerDesc.contains("store") ||
                        lowerDesc.contains("mall") || lowerDesc.contains("amazon") ||
                        lowerDesc.contains("flipkart") || lowerDesc.contains("clothes") -> SHOPPING

                // this is for BILLS!
                lowerDesc.contains("electric") || lowerDesc.contains("water") ||
                        lowerDesc.contains("internet") || lowerDesc.contains("phone") ||
                        lowerDesc.contains("wifi") || lowerDesc.contains("bill") -> BILLS

                // this is for ENTERTAINMENT!
                lowerDesc.contains("movie") || lowerDesc.contains("concert") ||
                        lowerDesc.contains("game") || lowerDesc.contains("netflix") ||
                        lowerDesc.contains("spotify") || lowerDesc.contains("cinema") -> ENTERTAINMENT

                // this is for TRANSPORT!
                lowerDesc.contains("uber") || lowerDesc.contains("taxi") ||
                        lowerDesc.contains("gas") || lowerDesc.contains("fuel") ||
                        lowerDesc.contains("bus") || lowerDesc.contains("train") ||
                        lowerDesc.contains("metro") || lowerDesc.contains("petrol") -> TRANSPORT

                // this is for EDUCATION!
                lowerDesc.contains("school") || lowerDesc.contains("course") ||
                        lowerDesc.contains("book") || lowerDesc.contains("tuition") ||
                        lowerDesc.contains("fee") || lowerDesc.contains("college") -> EDUCATION

                // this is for HEALTHCARE!
                lowerDesc.contains("doctor") || lowerDesc.contains("hospital") ||
                        lowerDesc.contains("medicine") || lowerDesc.contains("pharmacy") ||
                        lowerDesc.contains("clinic") || lowerDesc.contains("medical") -> HEALTHCARE

                // this is for TRAVEL!
                lowerDesc.contains("flight") || lowerDesc.contains("hotel") ||
                        lowerDesc.contains("vacation") || lowerDesc.contains("trip") ||
                        lowerDesc.contains("tour") || lowerDesc.contains("airbnb") -> TRAVEL

                // If nothing matches, we just put it in OTHER
                // This is the catch-all category for random stuff
                else -> OTHER
            }
        }
    }
}


