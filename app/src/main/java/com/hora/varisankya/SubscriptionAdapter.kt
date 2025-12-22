package com.hora.varisankya

import android.content.res.ColorStateList
import android.graphics.Color
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class SubscriptionAdapter(
    private val subscriptions: List<Subscription>,
    private val onSubscriptionClicked: (Subscription) -> Unit
) : RecyclerView.Adapter<SubscriptionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.subscription_name)
        val daysLeftTextView: TextView = view.findViewById(R.id.subscription_days_left)
        val detailsTextView: TextView = view.findViewById(R.id.subscription_details)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subscription, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val subscription = subscriptions[position]
        
        holder.nameTextView.text = subscription.name
        
        subscription.dueDate?.let { dueDate ->
            val format = SimpleDateFormat("MMM dd", Locale.getDefault())
            holder.detailsTextView.text = "Due ${format.format(dueDate)} • ${subscription.recurrence}"

            // Days left logic
            val today = Calendar.getInstance()
            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)

            val dueCal = Calendar.getInstance()
            dueCal.time = dueDate
            dueCal.set(Calendar.HOUR_OF_DAY, 0)
            dueCal.set(Calendar.MINUTE, 0)
            dueCal.set(Calendar.SECOND, 0)
            dueCal.set(Calendar.MILLISECOND, 0)

            val diff = dueCal.timeInMillis - today.timeInMillis
            val daysLeft = TimeUnit.MILLISECONDS.toDays(diff).toInt()

            val (text, bgColorAttr, textColorAttr) = when {
                daysLeft < 0 -> Triple(
                    "${-daysLeft}d Overdue",
                    com.google.android.material.R.attr.colorErrorContainer,
                    com.google.android.material.R.attr.colorOnErrorContainer
                )
                daysLeft == 0 -> Triple(
                    "Today",
                    com.google.android.material.R.attr.colorErrorContainer,
                    com.google.android.material.R.attr.colorOnErrorContainer
                )
                daysLeft == 1 -> Triple(
                    "Tomorrow",
                    com.google.android.material.R.attr.colorTertiaryContainer,
                    com.google.android.material.R.attr.colorOnTertiaryContainer
                )
                else -> Triple(
                    "$daysLeft Days",
                    if (daysLeft <= 7) com.google.android.material.R.attr.colorSecondaryContainer else com.google.android.material.R.attr.colorSurfaceContainerHigh,
                    if (daysLeft <= 7) com.google.android.material.R.attr.colorOnSecondaryContainer else com.google.android.material.R.attr.colorOnSurface
                )
            }

            holder.daysLeftTextView.text = text
            val bgColor = MaterialColors.getColor(holder.itemView, bgColorAttr)
            val textColor = MaterialColors.getColor(holder.itemView, textColorAttr)
            
            holder.daysLeftTextView.backgroundTintList = ColorStateList.valueOf(bgColor)
            holder.daysLeftTextView.setTextColor(textColor)
            holder.daysLeftTextView.visibility = View.VISIBLE
        } ?: run {
            holder.daysLeftTextView.visibility = View.GONE
            holder.detailsTextView.text = subscription.recurrence
        }

        holder.itemView.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            onSubscriptionClicked(subscription)
        }
    }

    override fun getItemCount() = subscriptions.size
    
    fun getItem(position: Int): Subscription = subscriptions[position]
}