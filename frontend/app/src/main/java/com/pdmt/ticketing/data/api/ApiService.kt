package com.pdmt.ticketing.data.api

import com.pdmt.ticketing.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/auth/me")
    suspend fun getMe(): Response<User>

    // Tickets
    @GET("api/tickets")
    suspend fun getTickets(
        @Query("status") status: String? = null,
        @Query("priority") priority: String? = null,
        @Query("search") search: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<TicketListResponse>

    @GET("api/tickets/{id}")
    suspend fun getTicket(@Path("id") id: Int): Response<Ticket>

    @POST("api/tickets")
    suspend fun createTicket(@Body request: CreateTicketRequest): Response<Ticket>

    @PATCH("api/tickets/{id}")
    suspend fun updateTicket(
        @Path("id") id: Int,
        @Body request: UpdateTicketRequest
    ): Response<Ticket>

    // Comments
    @GET("api/tickets/{id}/comments")
    suspend fun getComments(@Path("id") ticketId: Int): Response<List<Comment>>

    @POST("api/tickets/{id}/comments")
    suspend fun addComment(
        @Path("id") ticketId: Int,
        @Body request: CreateCommentRequest
    ): Response<Comment>
}