package com.pdmt.ticketing.ui.ticket

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.pdmt.ticketing.R
import com.pdmt.ticketing.utils.TokenManager
import com.pdmt.ticketing.viewmodel.TicketState
import com.pdmt.ticketing.viewmodel.TicketViewModel

class TicketListActivity : AppCompatActivity() {

    private lateinit var rvTickets: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var tvUserName: TextView
    private lateinit var etSearch: EditText
    private lateinit var fabCreateTicket: ExtendedFloatingActionButton
    private lateinit var chipAll: TextView
    private lateinit var chipOpen: TextView
    private lateinit var chipOnProgress: TextView
    private lateinit var chipResolved: TextView

    private lateinit var ticketViewModel: TicketViewModel
    private lateinit var ticketAdapter: TicketAdapter
    private lateinit var tokenManager: TokenManager

    private var currentFilter: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_list)

        tokenManager = TokenManager(applicationContext)
        ticketViewModel = ViewModelProvider(this)[TicketViewModel::class.java]

        // Bind views
        rvTickets = findViewById(R.id.rvTickets)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty = findViewById(R.id.tvEmpty)
        tvUserName = findViewById(R.id.tvUserName)
        etSearch = findViewById(R.id.etSearch)
        fabCreateTicket = findViewById(R.id.fabCreateTicket)
        chipAll = findViewById(R.id.chipAll)
        chipOpen = findViewById(R.id.chipOpen)
        chipOnProgress = findViewById(R.id.chipOnProgress)
        chipResolved = findViewById(R.id.chipResolved)

        // Setup RecyclerView
        ticketAdapter = TicketAdapter(emptyList()) { ticket ->
            val intent = Intent(this, TicketDetailActivity::class.java)
            intent.putExtra("ticket_id", ticket.id)
            startActivity(intent)
        }
        rvTickets.layoutManager = LinearLayoutManager(this)
        rvTickets.adapter = ticketAdapter

        // Tampilkan nama user
        kotlinx.coroutines.MainScope().launch {
            val name = tokenManager.getUserName()
            tvUserName.text = name ?: ""
        }

        // Observasi tickets
        ticketViewModel.tickets.observe(this) { tickets ->
            ticketAdapter.updateData(tickets)
            tvEmpty.visibility = if (tickets.isEmpty()) View.VISIBLE else View.GONE
            rvTickets.visibility = if (tickets.isEmpty()) View.GONE else View.VISIBLE
        }

        // Observasi state
        ticketViewModel.ticketState.observe(this) { state ->
            when (state) {
                is TicketState.Loading -> {
                    progressBar.visibility = View.VISIBLE
                }
                else -> {
                    progressBar.visibility = View.GONE
                }
            }
        }

        // Search
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                ticketViewModel.searchTickets(s.toString())
            }
        })

        // Filter chips
        chipAll.setOnClickListener { setFilter(null) }
        chipOpen.setOnClickListener { setFilter("open") }
        chipOnProgress.setOnClickListener { setFilter("on_progress") }
        chipResolved.setOnClickListener { setFilter("resolved") }

        // FAB buat tiket baru
        fabCreateTicket.setOnClickListener {
            startActivity(Intent(this, CreateTicketActivity::class.java))
        }

        // Load tiket
        ticketViewModel.getTickets()
    }

    override fun onResume() {
        super.onResume()
        ticketViewModel.getTickets(status = currentFilter)
    }

    private fun setFilter(status: String?) {
        currentFilter = status
        ticketViewModel.filterByStatus(status)

        // Update chip UI
        val activeColor = 0xFF2563EB.toInt()
        val inactiveColor = 0xFF374151.toInt()

        chipAll.setTextColor(if (status == null) 0xFFFFFFFF.toInt() else inactiveColor)
        chipOpen.setTextColor(if (status == "open") 0xFFFFFFFF.toInt() else inactiveColor)
        chipOnProgress.setTextColor(if (status == "on_progress") 0xFFFFFFFF.toInt() else inactiveColor)
        chipResolved.setTextColor(if (status == "resolved") 0xFFFFFFFF.toInt() else inactiveColor)

        chipAll.setBackgroundResource(if (status == null) R.drawable.chip_active else R.drawable.chip_inactive)
        chipOpen.setBackgroundResource(if (status == "open") R.drawable.chip_active else R.drawable.chip_inactive)
        chipOnProgress.setBackgroundResource(if (status == "on_progress") R.drawable.chip_active else R.drawable.chip_inactive)
        chipResolved.setBackgroundResource(if (status == "resolved") R.drawable.chip_active else R.drawable.chip_inactive)
    }
}