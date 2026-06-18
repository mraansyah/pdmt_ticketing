package com.pdmt.ticketing.ui.ticket

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pdmt.ticketing.R
import com.pdmt.ticketing.data.model.Ticket
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class TicketAdapter(
    private var tickets: List<Ticket>,
    private val onItemClick: (Ticket) -> Unit
) : RecyclerView.Adapter<TicketAdapter.TicketViewHolder>() {

    inner class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTicketNumber: TextView = itemView.findViewById(R.id.tvTicketNumber)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        val tvSite: TextView = itemView.findViewById(R.id.tvSite)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ticket, parent, false)
        return TicketViewHolder(view)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val ticket = tickets[position]

        holder.tvTicketNumber.text = ticket.ticketNumber
        holder.tvTitle.text = ticket.title
        holder.tvSite.text = "${ticket.site?.name ?: "Unknown"}"
        holder.tvTime.text = formatTime(ticket.createdAt)

        // Set status badge
        when (ticket.status) {
            "open" -> {
                holder.tvStatus.text = "Open"
                holder.tvStatus.setTextColor(0xFF1D4ED8.toInt())
                holder.tvStatus.setBackgroundResource(R.drawable.badge_open)
            }
            "on_progress" -> {
                holder.tvStatus.text = "On Progress"
                holder.tvStatus.setTextColor(0xFF92400E.toInt())
                holder.tvStatus.setBackgroundResource(R.drawable.badge_progress)
            }
            "resolved" -> {
                holder.tvStatus.text = "Resolved"
                holder.tvStatus.setTextColor(0xFF065F46.toInt())
                holder.tvStatus.setBackgroundResource(R.drawable.badge_resolved)
            }
            "closed" -> {
                holder.tvStatus.text = "Closed"
                holder.tvStatus.setTextColor(0xFF374151.toInt())
                holder.tvStatus.setBackgroundResource(R.drawable.badge_closed)
            }
        }

        holder.itemView.setOnClickListener { onItemClick(ticket) }
    }

    override fun getItemCount() = tickets.size

    fun updateData(newTickets: List<Ticket>) {
        tickets = newTickets
        notifyDataSetChanged()
    }

    private fun formatTime(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateStr) ?: return dateStr

            val now = System.currentTimeMillis()
            val diff = now - date.time

            when {
                diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m lalu"
                diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}j lalu"
                diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} hari lalu"
                else -> {
                    val outFormat = SimpleDateFormat("dd MMM", Locale("id"))
                    outFormat.format(date)
                }
            }
        } catch (e: Exception) {
            dateStr
        }
    }
}