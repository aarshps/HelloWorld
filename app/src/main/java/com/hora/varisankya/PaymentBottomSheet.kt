package com.hora.varisankya

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class PaymentBottomSheet(
    private val subscription: Subscription,
    private val onPaymentRecorded: () -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var historyRecycler: RecyclerView
    private lateinit var textNoHistory: TextView
    private lateinit var progressHistory: ProgressBar
    private lateinit var btnPayCurrent: Button
    private lateinit var btnPayCustom: Button
    private lateinit var textDueInfo: TextView
    private lateinit var textNextPreview: TextView

    private var currentDueDate: Date? = null
    private var projectedNextDate: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_payment, container, false)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        historyRecycler = view.findViewById(R.id.recycler_history)
        textNoHistory = view.findViewById(R.id.text_no_history)
        progressHistory = view.findViewById(R.id.progress_history)
        btnPayCurrent = view.findViewById(R.id.btn_pay_current)
        btnPayCustom = view.findViewById(R.id.btn_pay_custom)
        textDueInfo = view.findViewById(R.id.text_due_date_info)
        textNextPreview = view.findViewById(R.id.text_next_due_date_preview)

        currentDueDate = subscription.dueDate ?: Date()
        setupUI()
        loadHistory()
        calculateDates(currentDueDate!!)

        return view
    }

    private fun setupUI() {
        historyRecycler.layoutManager = LinearLayoutManager(context)

        btnPayCurrent.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            } else {
                it.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
            recordPayment(currentDueDate!!, projectedNextDate)
        }

        btnPayCustom.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Payment Date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

            datePicker.addOnPositiveButtonClickListener { ts ->
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.timeInMillis = ts
                val selectedDate = calendar.time
                val next = calculateNextDueDate(selectedDate, subscription.recurrence)
                recordPayment(selectedDate, next)
            }
            datePicker.show(childFragmentManager, "PAY_DATE_PICKER")
        }
    }

    private fun calculateDates(baseDate: Date) {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        textDueInfo.text = "Due: ${dateFormat.format(baseDate)}"
        
        projectedNextDate = calculateNextDueDate(baseDate, subscription.recurrence)
        projectedNextDate?.let {
            textNextPreview.text = "Next bill will be: ${dateFormat.format(it)}"
        } ?: run {
             textNextPreview.text = "Next due date: Undefined (Custom/None)"
        }
    }

    private fun calculateNextDueDate(fromDate: Date, recurrence: String): Date? {
        val cal = Calendar.getInstance()
        cal.time = fromDate
        cal.set(Calendar.HOUR_OF_DAY, 12)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        if (recurrence == "Custom") return null

        if (recurrence.startsWith("Every ")) {
            val parts = recurrence.split(" ")
            if (parts.size >= 3) {
                val freq = parts[1].toIntOrNull() ?: 1
                val unit = parts[2]
                when (unit) {
                    "Months", "Month" -> cal.add(Calendar.MONTH, freq)
                    "Years", "Year" -> cal.add(Calendar.YEAR, freq)
                    "Weeks", "Week" -> cal.add(Calendar.WEEK_OF_YEAR, freq)
                    "Days", "Day" -> cal.add(Calendar.DAY_OF_YEAR, freq)
                    else -> cal.add(Calendar.MONTH, freq)
                }
            }
        } else {
             when (recurrence) {
                "Monthly" -> cal.add(Calendar.MONTH, 1)
                "Yearly" -> cal.add(Calendar.YEAR, 1)
                "Weekly" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                "Daily" -> cal.add(Calendar.DAY_OF_YEAR, 1)
                else -> cal.add(Calendar.MONTH, 1)
            }
        }
        return cal.time
    }

    private fun loadHistory() {
        val userId = auth.currentUser?.uid ?: return
        val subId = subscription.id ?: run {
            progressHistory.visibility = View.GONE
            textNoHistory.visibility = View.VISIBLE
            textNoHistory.text = "Error: Subscription ID missing"
            return
        }

        progressHistory.visibility = View.VISIBLE
        textNoHistory.visibility = View.GONE

        firestore.collection("users").document(userId)
            .collection("subscriptions").document(subId)
            .collection("payments")
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { snapshots ->
                if (!isAdded) return@addOnSuccessListener
                progressHistory.visibility = View.GONE
                val payments = snapshots.toObjects(PaymentRecord::class.java)
                if (payments.isEmpty()) {
                    historyRecycler.visibility = View.GONE
                    textNoHistory.visibility = View.VISIBLE
                    textNoHistory.text = "No Payment History"
                } else {
                    historyRecycler.visibility = View.VISIBLE
                    textNoHistory.visibility = View.GONE
                    historyRecycler.adapter = PaymentAdapter(payments, subscription.currency)
                }
            }
            .addOnFailureListener { e ->
                if (!isAdded) return@addOnFailureListener
                Log.e("PaymentBottomSheet", "History Error: ${e.message}", e)
                // Fallback: try without ordering in case index is missing
                loadHistorySimple(userId, subId)
            }
    }

    private fun loadHistorySimple(userId: String, subId: String) {
        firestore.collection("users").document(userId)
            .collection("subscriptions").document(subId)
            .collection("payments")
            .limit(10)
            .get()
            .addOnSuccessListener { snapshots ->
                if (!isAdded) return@addOnSuccessListener
                progressHistory.visibility = View.GONE
                val payments = snapshots.toObjects(PaymentRecord::class.java).sortedByDescending { it.date }
                if (payments.isEmpty()) {
                    historyRecycler.visibility = View.GONE
                    textNoHistory.visibility = View.VISIBLE
                } else {
                    historyRecycler.visibility = View.VISIBLE
                    textNoHistory.visibility = View.GONE
                    historyRecycler.adapter = PaymentAdapter(payments, subscription.currency)
                }
            }
            .addOnFailureListener { e ->
                if (!isAdded) return@addOnFailureListener
                progressHistory.visibility = View.GONE
                textNoHistory.visibility = View.VISIBLE
                textNoHistory.text = "Permission Denied: Ensure Firestore rules allow access."
            }
    }

    private fun recordPayment(paymentDate: Date, nextDueDate: Date?) {
        val userId = auth.currentUser?.uid ?: return
        val subId = subscription.id ?: run {
            Toast.makeText(context, "Error: Subscription ID missing", Toast.LENGTH_SHORT).show()
            return
        }

        btnPayCurrent.isEnabled = false
        btnPayCustom.isEnabled = false

        val payment = PaymentRecord(
            date = paymentDate,
            amount = subscription.cost
        )

        val batch = firestore.batch()
        val subRef = firestore.collection("users").document(userId).collection("subscriptions").document(subId)
        val paymentRef = subRef.collection("payments").document()

        batch.set(paymentRef, payment)
        if (nextDueDate != null) {
            batch.update(subRef, "dueDate", nextDueDate)
        }

        batch.commit().addOnSuccessListener {
            if (isAdded) {
                onPaymentRecorded()
                dismiss()
            }
        }.addOnFailureListener { e ->
            if (isAdded) {
                btnPayCurrent.isEnabled = true
                btnPayCustom.isEnabled = true
                Log.e("PaymentBottomSheet", "Payment Failed", e)
                Toast.makeText(context, "Failed: ${e.message}. Check Firestore Rules.", Toast.LENGTH_LONG).show()
            }
        }
    }
}