package com.pdmt.ticketing.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: User
)

data class User(
    val id: Int,
    @SerializedName("site_id") val siteId: Int?,
    @SerializedName("site_name") val siteName: String?,
    val name: String,
    val username: String,
    val email: String,
    val role: String,
    @SerializedName("is_active") val isActive: Boolean
)

data class Site(
    val id: Int,
    val name: String,
    val code: String,
    val location: String,
    @SerializedName("is_active") val isActive: Boolean
)

data class Category(
    val id: Int,
    val name: String,
    val color: String,
    @SerializedName("is_active") val isActive: Boolean
)

data class Ticket(
    val id: Int,
    @SerializedName("ticket_number") val ticketNumber: String,
    @SerializedName("site_id") val siteId: Int,
    val site: Site?,
    @SerializedName("category_id") val categoryId: Int?,
    val category: Category?,
    @SerializedName("created_by") val createdBy: Int,
    val creator: User?,
    @SerializedName("assigned_to") val assignedTo: Int?,
    val assignee: User?,
    val title: String,
    val description: String,
    val status: String,
    val priority: String,
    @SerializedName("resolved_at") val resolvedAt: String?,
    @SerializedName("closed_at") val closedAt: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val comments: List<Comment>?
)

data class Comment(
    val id: Int,
    @SerializedName("ticket_id") val ticketId: Int,
    @SerializedName("user_id") val userId: Int?,
    val user: User?,
    val type: String,
    val content: String,
    @SerializedName("created_at") val createdAt: String
)

data class TicketListResponse(
    val data: List<Ticket>,
    val total: Int,
    val page: Int,
    val limit: Int
)

data class CreateTicketRequest(
    @SerializedName("category_id") val categoryId: Int?,
    @SerializedName("assigned_to") val assignedTo: Int?,
    val title: String,
    val description: String,
    val priority: String
)

data class UpdateTicketRequest(
    val status: String? = null,
    @SerializedName("assigned_to") val assignedTo: Int? = null,
    val priority: String? = null
)

data class CreateCommentRequest(
    val content: String
)