package com.hora.varisankya

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.text.SimpleDateFormat
import java.util.Calendar
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
        val progressBar: LinearProgressIndicator = view.findViewById(R.id.subscription_progress)
        val pillContainer: MaterialCardView = view.findViewById(R.id.unified_status_pill)
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

            val text = when {
                daysLeft < 0 -> "${-daysLeft}d Overdue"
                daysLeft == 0 -> "Today"
                daysLeft == 1 -> "Tomorrow"
                else -> "$daysLeft Days"
            }

            holder.daysLeftTextView.text = text
            holder.pillContainer.visibility = View.VISIBLE

            // Progress Bar Logic (Only 5 or fewer days left)
            val progress = when {
                daysLeft < 0 -> 100
                daysLeft <= 5 -> ((5 - daysLeft).toDouble() / 5.0 * 100).toInt()
                else -> 0
            }
            
            holder.progressBar.setProgress(progress, true)
            
            // M3E Dynamic Styling using Secondary Palette as requested
            val context = holder.itemView.context
            
            // Primary/Secondary Tonal Palette access
            val secondary = MaterialColors.getColor(context, com.google.android.material.R.attr.colorSecondary, Color.GRAY)
            val secondaryContainer = MaterialColors.getColor(context, com.google.android.material.R.attr.colorSecondaryContainer, Color.LTGRAY)
            val onSecondaryContainer = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSecondaryContainer, Color.BLACK)
            
            when {
                daysLeft < 0 -> {
                    // Overdue State: Use Error Container (Still urgent but unified)
                    val errorContainer = MaterialColors.getColor(context, com.google.android.material.R.attr.colorErrorContainer, Color.RED)
                    val onErrorContainer = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnErrorContainer, Color.BLACK)
                    val errorColor = MaterialColors.getColor(context, com.google.android.material.R.attr.colorError, Color.RED)
                    
                    holder.pillContainer.setCardBackgroundColor(ColorStateList.valueOf(errorContainer))
                    holder.daysLeftTextView.setTextColor(onErrorContainer)
                    holder.progressBar.setIndicatorColor(errorColor)
                    holder.progressBar.trackColor = MaterialColors.compositeARGBWithAlpha(errorColor, 32)
                }
                daysLeft == 0 -> {
                    // Today State: High contrast Secondary
                    holder.pillContainer.setCardBackgroundColor(ColorStateList.valueOf(secondary))
                    holder.daysLeftTextView.setTextColor(MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSecondary, Color.BLACK))
                    holder.progressBar.setIndicatorColor(MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSecondary, Color.WHITE))
                    holder.progressBar.trackColor = MaterialColors.compositeARGBWithAlpha(Color.WHITE, 64)
                }
                else -> {
                    // Normal State: Clean, high-contrast Secondary Container
                    holder.pillContainer.setCardBackgroundColor(ColorStateList.valueOf(secondaryContainer))
                    holder.daysLeftTextView.setTextColor(onSecondaryContainer)
                    holder.progressBar.setIndicatorColor(secondary)
                    holder.progressBar.trackColor = MaterialColors.compositeARGBWithAlpha(secondary, 32)
                }
            }

        } ?: run {
            holder.pillContainer.visibility = View.GONE
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