package com.hora.varisankya

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.color.MaterialColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class SubscriptionAdapter(
    private var subscriptions: List<Subscription>,
    private val onSubscriptionClicked: (Subscription) -> Unit
) : RecyclerView.Adapter<SubscriptionAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.subscription_name)
        val daysLeftTextView: TextView = view.findViewById(R.id.subscription_days_left)
        val detailsTextView: TextView = view.findViewById(R.id.subscription_details)
        val pillContainer: MaterialCardView = view.findViewById(R.id.unified_status_pill)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subscription, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val subscription = subscriptions[position]
        val context = holder.itemView.context
        
        holder.nameTextView.text = subscription.name
        
        if (!subscription.active) {
            holder.itemView.alpha = 0.6f
            holder.nameTextView.setTextColor(MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY))
            holder.detailsTextView.text = "Discontinued • ${subscription.recurrence}"
            holder.pillContainer.visibility = View.VISIBLE
            holder.daysLeftTextView.text = "Inactive"
            
            // Inactive style
            val surfaceContainer = MaterialColors.getColor(context, com.google.android.material.R.attr.colorSurfaceContainerHigh, Color.LTGRAY)
            val onSurfaceVariant = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY)
            
            holder.pillContainer.setCardBackgroundColor(ColorStateList.valueOf(surfaceContainer))
            holder.daysLeftTextView.setTextColor(onSurfaceVariant)
            
        } else {
            holder.itemView.alpha = 1.0f
            holder.nameTextView.setTextColor(MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurface, Color.BLACK))
            
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
                
                // M3E Dynamic Styling
                val secondary = MaterialColors.getColor(context, com.google.android.material.R.attr.colorSecondary, Color.GRAY)
                val onSecondary = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSecondary, Color.WHITE)
                val secondaryContainer = MaterialColors.getColor(context, com.google.android.material.R.attr.colorSecondaryContainer, Color.LTGRAY)
                val onSecondaryContainer = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSecondaryContainer, Color.BLACK)
                
                when {
                    daysLeft < 0 -> {
                        val errorContainer = MaterialColors.getColor(context, com.google.android.material.R.attr.colorErrorContainer, Color.RED)
                        val onErrorContainer = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnErrorContainer, Color.BLACK)

                        holder.pillContainer.setCardBackgroundColor(ColorStateList.valueOf(errorContainer))
                        holder.daysLeftTextView.setTextColor(onErrorContainer)
                    }
                    daysLeft == 0 -> {
                        holder.pillContainer.setCardBackgroundColor(ColorStateList.valueOf(secondary))
                        holder.daysLeftTextView.setTextColor(onSecondary)
                    }
                    else -> {
                        holder.pillContainer.setCardBackgroundColor(ColorStateList.valueOf(secondaryContainer))
                        holder.daysLeftTextView.setTextColor(onSecondaryContainer)
                    }
                }

            } ?: run {
                holder.pillContainer.visibility = View.GONE
                holder.detailsTextView.text = subscription.recurrence
            }
        }

        holder.itemView.setOnClickListener {
            val haptic = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.VIRTUAL_KEY
            PreferenceHelper.performHaptics(it, haptic)
            onSubscriptionClicked(subscription)
        }
    }

    override fun getItemCount() = subscriptions.size

    fun updateData(newSubscriptions: List<Subscription>) {
        val diffCallback = object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = subscriptions.size
            override fun getNewListSize(): Int = newSubscriptions.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                // Use document ID for identity comparison
                return subscriptions[oldItemPosition].id == newSubscriptions[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return subscriptions[oldItemPosition] == newSubscriptions[newItemPosition]
            }
        }

        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.subscriptions = newSubscriptions
        diffResult.dispatchUpdatesTo(this)
    }
}