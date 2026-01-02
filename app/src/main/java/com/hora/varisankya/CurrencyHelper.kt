package com.hora.varisankya

import java.util.Locale

object CurrencyHelper {

    // Base Currency: USD
    // approximate rates as of early 2026 (projected/stable averages)
    private val rates = mapOf(
        "USD" to 1.0,
        "EUR" to 0.92,
        "GBP" to 0.78,
        "INR" to 85.0,
        "JPY" to 150.0,
        "CAD" to 1.35,
        "AUD" to 1.50,
        "CNY" to 7.20,
        "CHF" to 0.88,
        "SGD" to 1.34
    )

    fun getSymbol(currencyCode: String): String {
        return try {
            java.util.Currency.getInstance(currencyCode).symbol
        } catch (e: Exception) {
            "$"
        }
    }

    /**
     * Converts an amount from one currency to another using the static rates.
     * If a currency is unknown, it falls back to 1:1 conversion (worst case safety).
     */
    fun convert(amount: Double, fromCurrency: String, toCurrency: String): Double {
        if (fromCurrency.equals(toCurrency, ignoreCase = true)) return amount

        val fromRate = rates[fromCurrency.uppercase(Locale.ROOT)] ?: 1.0
        val toRate = rates[toCurrency.uppercase(Locale.ROOT)] ?: 1.0

        // Convert to USD first (Amount / Rate), then to Target (USD * TargetRate)
        val amountInUSD = amount / fromRate
        return amountInUSD * toRate
    }
}
