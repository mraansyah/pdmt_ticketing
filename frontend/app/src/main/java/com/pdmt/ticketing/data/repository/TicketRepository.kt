package com.pdmt.ticketing.data.repository

import com.pdmt.ticketing.data.api.RetrofitClient
import com.pdmt.ticketing.data.model.*

class TicketRepository {

    private val api = RetrofitClient.instance

    // Auth
    suspend fun login(username: String, password: String): Result<LoginResponse> {
        return try {
            val response = api.login(LoginRequest(username, password))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Username atau password salah"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Tidak dapat terhubung ke server"))
        }
    }

    suspend fun getMe(): Result<User> {
        return try {
            val response = api.getMe()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Gagal ambil profil"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Tidak dapat terhubung ke server"))
        }
    }

    // Tickets
    suspend fun getTickets(
        status: String? = null,
        priority: String? = null,
        search: String? = null,
        page: Int = 1
    ): Result<TicketListResponse> {
        return try {
            val response = api.getTickets(status, priority, search, page)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Gagal mengambil data tiket"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Tidak dapat terhubung ke server"))
        }
    }

    suspend fun getTicket(id: Int): Result<Ticket> {
        return try {
            val response = api.getTicket(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Tiket tidak ditemukan"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Tidak dapat terhubung ke server"))
        }
    }

    suspend fun createTicket(request: CreateTicketRequest): Result<Ticket> {
        return try {
            val response = api.createTicket(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Gagal membuat tiket"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Tidak dapat terhubung ke server"))
        }
    }

    suspend fun updateTicket(id: Int, request: UpdateTicketRequest): Result<Ticket> {
        return try {
            val response = api.updateTicket(id, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Gagal update tiket"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Tidak dapat terhubung ke server"))
        }
    }

    // Comments
    suspend fun getComments(ticketId: Int): Result<List<Comment>> {
        return try {
            val response = api.getComments(ticketId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Gagal mengambil komentar"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Tidak dapat terhubung ke server"))
        }
    }

    suspend fun addComment(ticketId: Int, content: String): Result<Comment> {
        return try {
            val response = api.addComment(ticketId, CreateCommentRequest(content))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Gagal menambah komentar"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Tidak dapat terhubung ke server"))
        }
    }
}