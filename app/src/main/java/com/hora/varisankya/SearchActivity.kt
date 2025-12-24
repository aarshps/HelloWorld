package com.hora.varisankya

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ContextThemeWrapper
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SearchActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    
    private lateinit var searchEditText: EditText
    private lateinit var categoryChipGroup: ChipGroup
    private lateinit var searchRecyclerView: RecyclerView
    
    private var allSubscriptions: List<Subscription> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        searchEditText = findViewById(R.id.search_edit_text)
        categoryChipGroup = findViewById(R.id.search_category_chip_group)
        searchRecyclerView = findViewById(R.id.search_recycler_view)

        searchRecyclerView.layoutManager = LinearLayoutManager(this)
        
        setupCategories()
        loadAllSubscriptions()

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                performSearch()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        categoryChipGroup.setOnCheckedStateChangeListener { _, _ ->
            performSearch()
        }
        
        searchEditText.requestFocus()
    }

    private fun setupCategories() {
        categoryChipGroup.removeAllViews()
        Constants.CATEGORIES.forEach { category ->
            val chip = Chip(ContextThemeWrapper(this, com.google.android.material.R.style.Widget_Material3_Chip_Filter)).apply {
                text = category
                isCheckable = true
                setOnClickListener {
                    PreferenceHelper.performHaptics(it, HapticFeedbackConstants.CLOCK_TICK)
                }
            }
            categoryChipGroup.addView(chip)
        }
    }

    private fun loadAllSubscriptions() {
        auth.currentUser?.uid?.let { userId ->
            firestore.collection("users").document(userId)
                .collection("subscriptions")
                .get()
                .addOnSuccessListener { snapshots ->
                    allSubscriptions = snapshots.toObjects(Subscription::class.java)
                    performSearch()
                }
        }
    }

    private fun performSearch() {
        val query = searchEditText.text.toString().lowercase().trim()
        val selectedCategories = getSelectedCategories()
        
        val filtered = allSubscriptions.filter { sub ->
            val matchText = sub.name.lowercase().contains(query) || 
                            sub.category.lowercase().contains(query)
            
            val matchCategory = if (selectedCategories.isEmpty()) true else sub.category in selectedCategories
            
            matchText && matchCategory
        }
        
        // Sort: Active first, then query match, then due date
        val sortedFiltered = filtered.sortedWith(
            compareByDescending<Subscription> { it.active }
                .thenByDescending { it.name.lowercase() == query }
                .thenBy { it.dueDate }
        )
        
        searchRecyclerView.adapter = SubscriptionAdapter(sortedFiltered) { subscription ->
            val addSubscriptionBottomSheet = AddSubscriptionBottomSheet(subscription) {
                loadAllSubscriptions() // Reload if changed
            }
            addSubscriptionBottomSheet.show(supportFragmentManager, "AddSubscriptionBottomSheet")
        }
    }

    private fun getSelectedCategories(): List<String> {
        val checkedIds = categoryChipGroup.checkedChipIds
        val categories = mutableListOf<String>()
        for (id in checkedIds) {
            val chip = categoryChipGroup.findViewById<Chip>(id)
            if (chip != null) {
                categories.add(chip.text.toString())
            }
        }
        return categories
    }

    override fun onSupportNavigateUp(): Boolean {
        window.decorView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        finish()
        return true
    }
}