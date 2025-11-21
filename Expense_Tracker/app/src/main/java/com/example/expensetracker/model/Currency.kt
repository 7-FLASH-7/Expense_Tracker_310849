package com.example.expensetracker.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

// Multi-currency support with 28+ currencies
@Parcelize
enum class Currency(val code: String, val symbol: String, val displayName: String) : Parcelable {
    USD("USD", "$", "US Dollar"),
    EUR("EUR", "€", "Euro"),
    GBP("GBP", "£", "British Pound"),
    JPY("JPY", "¥", "Japanese Yen"),
    INR("INR", "₹", "Indian Rupee"),
    BDT("BDT", "৳", "Bangladeshi Taka"),
    CNY("CNY", "¥", "Chinese Yuan"),
    AUD("AUD", "A$", "Australian Dollar"),
    CAD("CAD", "C$", "Canadian Dollar"),
    CHF("CHF", "Fr", "Swiss Franc"),
    HKD("HKD", "HK$", "Hong Kong Dollar"),
    SGD("SGD", "S$", "Singapore Dollar"),
    SEK("SEK", "kr", "Swedish Krona"),
    KRW("KRW", "₩", "South Korean Won"),
    NOK("NOK", "kr", "Norwegian Krone"),
    NZD("NZD", "NZ$", "New Zealand Dollar"),
    MXN("MXN", "$", "Mexican Peso"),
    ZAR("ZAR", "R", "South African Rand"),
    BRL("BRL", "R$", "Brazilian Real"),
    RUB("RUB", "₽", "Russian Ruble"),
    TRY("TRY", "₺", "Turkish Lira"),
    THB("THB", "฿", "Thai Baht"),
    IDR("IDR", "Rp", "Indonesian Rupiah"),
    MYR("MYR", "RM", "Malaysian Ringgit"),
    PHP("PHP", "₱", "Philippine Peso"),
    VND("VND", "₫", "Vietnamese Dong"),
    PKR("PKR", "₨", "Pakistani Rupee"),
    AED("AED", "د.إ", "UAE Dirham");

    companion object {
        fun fromCode(code: String): Currency {
            return values().find { it.code == code } ?: EUR
        }
    }
}