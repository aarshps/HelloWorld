package com.hora.varisankya

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import com.google.android.material.color.MaterialColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

class PaymentHistoryChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Currency
    var currencySymbol: String = "$"

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val labelBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#15424242") // fallback
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 30f
        textAlign = Paint.Align.CENTER
        // Set font to sans-serif-medium
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    }

    private val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 28f
        textAlign = Paint.Align.CENTER
        color = Color.GRAY
    }

    // Pair of Label (Date/Name), Value, and original Data Object (for click handling)
    private val dataPoints = mutableListOf<Triple<String, Double, Any?>>()
    private var onBarClickListener: ((Any?) -> Unit)? = null

    // Dimensions
    private val barWidth = 120f // Slightly wider bars for better touch area
    private val barSpacing = 60f 
    private val chartPaddingTop = 120f 
    private val chartPaddingBottom = 160f // Increased space for rotated labels
    private val labelPadding = 16f
    private val cornerRadius = 60f 

    fun setPaymentData(payments: List<PaymentRecord>) {
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val newData = payments.sortedBy { it.date }.map { 
            Triple(dateFormat.format(it.date ?: Date()), it.amount ?: 0.0, it as Any?) 
        }
        animateDataUpdate(newData)
    }

    fun setChartData(data: List<Triple<String, Double, Any?>>) {
        animateDataUpdate(data)
    }

    fun setOnBarClickListener(listener: (Any?) -> Unit) {
        this.onBarClickListener = listener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val minWidthView = MeasureSpec.getSize(widthMeasureSpec)

        // Calculate needed width: bars + spacing + extra buffer for last rotated label
        // Adding 150f buffer at end to prevent clipping of slanted text
        val contentWidth = if (dataPoints.isNotEmpty()) {
            (dataPoints.size * (barWidth + barSpacing) + barSpacing + 150f).toInt()
        } else {
            0
        }

        // If content is smaller than screen, fill screen (minWidthView).
        // If content is larger, expand to scroll (contentWidth).
        val finalWidth = max(minWidthView, contentWidth)
        
        setMeasuredDimension(finalWidth, heightSize)
    }

    // Animation State
    private var animationProgress = 1f
    private var isAnimating = false
    private var oldDataPoints = listOf<Triple<String, Double, Any?>>()

    // ... (existing helper methods)

    fun animateDataUpdate(newData: List<Triple<String, Double, Any?>>) {
        oldDataPoints = ArrayList(dataPoints)
        dataPoints.clear()
        dataPoints.addAll(newData)
        
        // CRITICAL: Request layout to recalculate width based on new data size!
        requestLayout()
        
        // If old data is empty, just fade/grow in normally? 
        // Or if sizes mismatch, we can't do 1:1 morph easily.
        // Simple approach: Animate "progress" from 0 to 1.
        // We will redraw bars from height 0 to target? 
        // Or better: Animate from previous state if possible, else 0.
        
        val animator = android.animation.ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 600 // Slower, more "deliberate" premium feel
        animator.interpolator = androidx.interpolator.view.animation.FastOutSlowInInterpolator()
        animator.addUpdateListener { 
            animationProgress = it.animatedValue as Float
            invalidate()
        }
        isAnimating = true
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (dataPoints.isEmpty()) return

        val width = width.toFloat()
        val height = height.toFloat()
        
        val availableHeight = height - chartPaddingTop - chartPaddingBottom

        // Resolve Colors
        val colorPrimary = MaterialColors.getColor(context, android.R.attr.colorPrimary, Color.BLUE)
        val colorSecondaryContainer = MaterialColors.getColor(context, com.google.android.material.R.attr.colorSecondaryContainer, Color.LTGRAY)
        val colorOnSecondaryContainer = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSecondaryContainer, Color.BLACK)
        
        barPaint.color = colorPrimary
        datePaint.color = colorOnSecondaryContainer
        labelBgPaint.color = colorSecondaryContainer
        textPaint.color = colorOnSecondaryContainer

        // Scaling
        val maxAmount = dataPoints.maxOf { it.second }.toFloat()
        val rangeY = max(maxAmount, 100f)

        // Calculate total content width correctly
        val totalContentWidth = (dataPoints.size * (barWidth + barSpacing)) + barSpacing
        
        // Center content if smaller
        val startOffset = if (totalContentWidth < width) {
            (width - totalContentWidth) / 2f
        } else {
            0f
        }

        dataPoints.forEachIndexed { index, triple ->
            val label = triple.first
            val rawAmount = triple.second.toFloat()
            
            // Animation Logic: Scale height by progress
            val amount = if (isAnimating) rawAmount * animationProgress else rawAmount

            // Calculate Position with Centering Offset
            val xCenter = startOffset + barSpacing + (index * (barWidth + barSpacing)) + (barWidth / 2)
            
            // Height logic
            val barHeight = (amount / rangeY) * availableHeight
            val visualBarHeight = max(barHeight, 20f) 

            val barTop = height - chartPaddingBottom - visualBarHeight
            val barBottom = height - chartPaddingBottom

            // Draw Bar
            val rect = RectF(
                xCenter - barWidth / 2,
                barTop,
                xCenter + barWidth / 2,
                barBottom
            )
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, barPaint)

            // --- Draw X-Axis Label (Date/Name) Rotated ---
            // Truncate logic: "Comfortable length" -> ~12 chars max
            var labelTextX = label
            if (labelTextX.length > 12) {
                labelTextX = labelTextX.take(10) + "..."
            }
            val dateTextWidth = datePaint.measureText(labelTextX)
            
            // Position label in the middle of bottom padding area
            val labelYCenter = height - (chartPaddingBottom / 2)
            
            // Save canvas for rotation
            canvas.save()
            // Pivot at the center of the text
            canvas.rotate(-45f, xCenter, labelYCenter)
            
            val dateBgRect = RectF(
                xCenter - dateTextWidth / 2 - labelPadding,
                labelYCenter - 30f, 
                xCenter + dateTextWidth / 2 + labelPadding,
                labelYCenter + 30f
            )
            // Draw Chip BG
            canvas.drawRoundRect(dateBgRect, 20f, 20f, labelBgPaint)
            // Draw Text
            val dateMetrics = datePaint.fontMetrics
            val dateBaseline = dateBgRect.centerY() - (dateMetrics.bottom + dateMetrics.top) / 2
            canvas.drawText(labelTextX, xCenter, dateBaseline, datePaint)
            
            canvas.restore()

            // --- Draw Amount Label (Top) ---
            // Only draw amount if bar is tall enough or always? 
            // Let's fade it in with animation
            if (animationProgress > 0.5f) {
                textPaint.alpha = (255 * (animationProgress - 0.5f) * 2).toInt()
                labelBgPaint.alpha = (255 * (animationProgress - 0.5f) * 2).toInt()
                
                val labelTextY = String.format("%s%.0f", currencySymbol, rawAmount)
                val textWidth = textPaint.measureText(labelTextY)
                val bgRect = RectF(
                    xCenter - textWidth / 2 - labelPadding,
                    barTop - 50f - labelPadding, 
                    xCenter + textWidth / 2 + labelPadding,
                    barTop - 50f + 30f + labelPadding 
                )
                canvas.drawRoundRect(bgRect, 20f, 20f, labelBgPaint)
                val fontMetrics = textPaint.fontMetrics
                val baseline = bgRect.centerY() - (fontMetrics.bottom + fontMetrics.top) / 2
                canvas.drawText(labelTextY, xCenter, baseline, textPaint)
                
                // Reset alpha
                textPaint.alpha = 255
                labelBgPaint.alpha = 255
            }
        }
    }

    private var lastDownX = 0f
    private var lastDownY = 0f
    
    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        if (onBarClickListener == null) return super.onTouchEvent(event)

        when (event.action) {
            android.view.MotionEvent.ACTION_DOWN -> {
                lastDownX = event.x
                lastDownY = event.y
                return true
            }
            android.view.MotionEvent.ACTION_UP -> {
                if (Math.abs(event.x - lastDownX) < 50 && Math.abs(event.y - lastDownY) < 50) {
                    handleTap(event.x)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun handleTap(x: Float) {
        val minWidth = measuredWidth.toFloat()
        val totalContentWidth = (dataPoints.size * (barWidth + barSpacing)) + barSpacing
        val startOffset = if (totalContentWidth < minWidth) {
            (minWidth - totalContentWidth) / 2f
        } else {
            0f
        }

        val relativeX = x - startOffset
        val widthPerItem = barWidth + barSpacing
        
        val index = ((relativeX - (barSpacing / 2)) / widthPerItem).toInt()
        
        if (index in 0 until dataPoints.size) {
            val item = dataPoints[index]
            PreferenceHelper.performHaptics(this, android.view.HapticFeedbackConstants.CONTEXT_CLICK)
            onBarClickListener?.invoke(item.third)
        }
    }
}
