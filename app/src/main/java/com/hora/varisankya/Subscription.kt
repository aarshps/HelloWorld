package com.hora.varisankya

import com.google.firebase.firestore.DocumentId
import java.util.Date

data class Subscription(
    @DocumentId val id: String? = null,
    val name: String = "",
    val dueDate: Date? = null,
    val cost: Double = 0.0,
    val currency: String = "USD",
    val recurrence: String = "Monthly",
    val category: String = "Entertainment"
)