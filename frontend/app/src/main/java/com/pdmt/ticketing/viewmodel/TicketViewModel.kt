package com.pdmt.ticketing.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdmt.ticketing.data.model.*
import com.pdmt.ticketing.data.repository.TicketRepository
import kotlinx.coroutines.launch

class TicketViewModel : ViewModel() {

    private val repository = TicketRepository()

    // Ticket list
    private val _tickets = MutableLiveData<List<Ticket>>()
    val tickets: LiveData<List<Ticket>> = _tickets

    private val _ticketState = MutableLiveData<TicketState>()
    val ticketState: LiveData<TicketState> = _ticketState

    // Ticket detail
    private val _selectedTicket = MutableLiveData<Ticket?>()
    val selectedTicket: LiveData<Ticket?> = _selectedTicket

    // Comments
    private val _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>> = _comments

    // Filter state
    private var currentStatus: String? = null
    private var currentSearch: String? = null

    fun getTickets(
        status: String? = currentStatus,
        search: String? = currentSearch,
        page: Int = 1
    ) {
        currentStatus = status
        currentSearch = search
        _ticketState.value = TicketState.Loading

        viewModelScope.launch {
            val result = repository.getTickets(status = status, search = search, page = page)
            result.fold(
                onSuccess = { response ->
                    _tickets.value = response.data
                    _ticketState.value = TicketState.Success
                },
                onFailure = { error ->
                    _ticketState.value = TicketState.Error(error.message ?: "Gagal memuat tiket")
                }
            )
        }
    }

    fun getTicketDetail(id: Int) {
        _ticketState.value = TicketState.Loading

        viewModelScope.launch {
            val result = repository.getTicket(id)
            result.fold(
                onSuccess = { ticket ->
                    _selectedTicket.value = ticket
                    _ticketState.value = TicketState.Success
                },
                onFailure = { error ->
                    _ticketState.value = TicketState.Error(error.message ?: "Gagal memuat detail tiket")
                }
            )
        }
    }

    fun createTicket(
        title: String,
        description: String,
        priority: String,
        categoryId: Int? = null
    ) {
        if (title.isBlank()) {
            _ticketState.value = TicketState.Error("Judul tiket wajib diisi")
            return
        }
        if (description.isBlank()) {
            _ticketState.value = TicketState.Error("Deskripsi wajib diisi")
            return
        }

        _ticketState.value = TicketState.Loading

        viewModelScope.launch {
            val request = CreateTicketRequest(
                categoryId = categoryId,
                assignedTo = null,
                title = title,
                description = description,
                priority = priority
            )
            val result = repository.createTicket(request)
            result.fold(
                onSuccess = {
                    _ticketState.value = TicketState.Created
                    getTickets() // refresh list
                },
                onFailure = { error ->
                    _ticketState.value = TicketState.Error(error.message ?: "Gagal membuat tiket")
                }
            )
        }
    }

    fun updateTicketStatus(id: Int, status: String) {
        _ticketState.value = TicketState.Loading

        viewModelScope.launch {
            val result = repository.updateTicket(id, UpdateTicketRequest(status = status))
            result.fold(
                onSuccess = { ticket ->
                    _selectedTicket.value = ticket
                    _ticketState.value = TicketState.Updated
                    getTickets() // refresh list
                },
                onFailure = { error ->
                    _ticketState.value = TicketState.Error(error.message ?: "Gagal update status")
                }
            )
        }
    }

    fun getComments(ticketId: Int) {
        viewModelScope.launch {
            val result = repository.getComments(ticketId)
            result.fold(
                onSuccess = { _comments.value = it },
                onFailure = { }
            )
        }
    }

    fun addComment(ticketId: Int, content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            val result = repository.addComment(ticketId, content)
            result.fold(
                onSuccess = {
                    getComments(ticketId) // refresh komentar
                    getTicketDetail(ticketId)
                },
                onFailure = { error ->
                    _ticketState.value = TicketState.Error(error.message ?: "Gagal kirim komentar")
                }
            )
        }
    }

    fun searchTickets(query: String) {
        getTickets(search = query.ifBlank { null })
    }

    fun filterByStatus(status: String?) {
        getTickets(status = status)
    }

    fun clearSelectedTicket() {
        _selectedTicket.value = null
    }
}

sealed class TicketState {
    object Loading : TicketState()
    object Success : TicketState()
    object Created : TicketState()
    object Updated : TicketState()
    data class Error(val message: String) : TicketState()
}