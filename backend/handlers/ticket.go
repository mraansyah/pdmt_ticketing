package handlers

import (
	"strconv"
	"time"

	"github.com/gofiber/fiber/v3"

	"pdmt-ticketing/config"
	"pdmt-ticketing/middleware"
	"pdmt-ticketing/models"
)

// GET /api/tickets
// Query params: status, priority, site_id, search, page, limit
func GetTickets(c fiber.Ctx) error {
	role := middleware.GetRole(c)
	siteID := middleware.GetSiteID(c)

	query := config.DB.Model(&models.Ticket{}).
		Preload("Site").
		Preload("Category").
		Preload("Creator").
		Preload("Assignee").
		Order("created_at DESC")

	// Role site hanya bisa lihat tiket dari site mereka sendiri
	if role == "site" && siteID != nil {
		query = query.Where("site_id = ?", *siteID)
	}

	// Filter opsional
	if status := c.Query("status"); status != "" {
		query = query.Where("status = ?", status)
	}
	if priority := c.Query("priority"); priority != "" {
		query = query.Where("priority = ?", priority)
	}
	if filterSiteID := c.Query("site_id"); filterSiteID != "" {
		query = query.Where("site_id = ?", filterSiteID)
	}

	// Search berdasarkan judul
	if search := c.Query("search"); search != "" {
		query = query.Where("title ILIKE ?", "%"+search+"%")
	}

	// Pagination
	page, _ := strconv.Atoi(c.Query("page", "1"))
	limit, _ := strconv.Atoi(c.Query("limit", "20"))
	if page < 1 {
		page = 1
	}
	offset := (page - 1) * limit

	var total int64
	query.Count(&total)

	var tickets []models.Ticket
	if err := query.Limit(limit).Offset(offset).Find(&tickets).Error; err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{
			"error": "Gagal mengambil data tiket",
		})
	}

	return c.JSON(fiber.Map{
		"data":  tickets,
		"total": total,
		"page":  page,
		"limit": limit,
	})
}

// GET /api/tickets/:id
func GetTicket(c fiber.Ctx) error {
	id := c.Params("id")

	var ticket models.Ticket
	if err := config.DB.
		Preload("Site").
		Preload("Category").
		Preload("Creator").
		Preload("Assignee").
		Preload("Comments").
		Preload("Comments.User").
		First(&ticket, id).Error; err != nil {
		return c.Status(fiber.StatusNotFound).JSON(fiber.Map{
			"error": "Tiket tidak ditemukan",
		})
	}

	return c.JSON(ticket)
}

// POST /api/tickets
func CreateTicket(c fiber.Ctx) error {
	userID := middleware.GetUserID(c)
	siteID := middleware.GetSiteID(c)
	role := middleware.GetRole(c)

	var req models.CreateTicketRequest
	if err := c.Bind().JSON(&req); err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": "Request tidak valid",
		})
	}

	if req.Title == "" || req.Description == "" {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": "Judul dan deskripsi wajib diisi",
		})
	}

	// Tentukan site_id: site user pakai site mereka sendiri, HO bisa pilih
	var ticketSiteID uint
	if role == "site" && siteID != nil {
		ticketSiteID = *siteID
	} else {
		// HO wajib kirim site_id di body
		if req.Title == "" {
			return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
				"error": "site_id wajib diisi",
			})
		}
		ticketSiteID = userID // fallback, sebaiknya HO kirim site_id
	}

	// Set default priority
	if req.Priority == "" {
		req.Priority = models.PriorityMedium
	}

	ticket := models.Ticket{
		SiteID:      ticketSiteID,
		CategoryID:  req.CategoryID,
		CreatedBy:   userID,
		AssignedTo:  req.AssignedTo,
		Title:       req.Title,
		Description: req.Description,
		Priority:    req.Priority,
		Status:      models.StatusOpen,
	}

	if err := config.DB.Create(&ticket).Error; err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{
			"error": "Gagal membuat tiket",
		})
	}

	// Tambah komentar sistem otomatis
	systemComment := models.Comment{
		TicketID: ticket.ID,
		UserID:   &userID,
		Type:     models.CommentTypeSystem,
		Content:  "Tiket dibuat",
	}
	config.DB.Create(&systemComment)

	// Load relasi untuk response
	config.DB.Preload("Site").Preload("Category").Preload("Creator").First(&ticket, ticket.ID)

	return c.Status(fiber.StatusCreated).JSON(ticket)
}

// PATCH /api/tickets/:id
func UpdateTicket(c fiber.Ctx) error {
	id := c.Params("id")
	userID := middleware.GetUserID(c)

	var ticket models.Ticket
	if err := config.DB.First(&ticket, id).Error; err != nil {
		return c.Status(fiber.StatusNotFound).JSON(fiber.Map{
			"error": "Tiket tidak ditemukan",
		})
	}

	var req models.UpdateTicketRequest
	if err := c.Bind().JSON(&req); err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": "Request tidak valid",
		})
	}

	oldStatus := ticket.Status

	// Update field yang dikirim
	if req.Status != "" {
		ticket.Status = req.Status

		// Set resolved_at / closed_at
		if req.Status == models.StatusResolved && ticket.ResolvedAt == nil {
			now := time.Now()
			ticket.ResolvedAt = &now
		}
		if req.Status == models.StatusClosed && ticket.ClosedAt == nil {
			now := time.Now()
			ticket.ClosedAt = &now
		}
	}
	if req.AssignedTo != nil {
		ticket.AssignedTo = req.AssignedTo
	}
	if req.CategoryID != nil {
		ticket.CategoryID = req.CategoryID
	}
	if req.Priority != "" {
		ticket.Priority = req.Priority
	}

	if err := config.DB.Save(&ticket).Error; err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{
			"error": "Gagal update tiket",
		})
	}

	// Tambah komentar riwayat perubahan status otomatis
	if req.Status != "" && oldStatus != req.Status {
		content := "Status diubah dari " + string(oldStatus) + " ke " + string(req.Status)
		comment := models.Comment{
			TicketID: ticket.ID,
			UserID:   &userID,
			Type:     models.CommentTypeStatusChange,
			Content:  content,
		}
		config.DB.Create(&comment)
	}

	config.DB.Preload("Site").Preload("Category").Preload("Creator").Preload("Assignee").First(&ticket, ticket.ID)

	return c.JSON(ticket)
}

// DELETE /api/tickets/:id — hanya admin
func DeleteTicket(c fiber.Ctx) error {
	id := c.Params("id")

	var ticket models.Ticket
	if err := config.DB.First(&ticket, id).Error; err != nil {
		return c.Status(fiber.StatusNotFound).JSON(fiber.Map{
			"error": "Tiket tidak ditemukan",
		})
	}

	config.DB.Delete(&ticket)

	return c.JSON(fiber.Map{
		"message": "Tiket berhasil dihapus",
	})
}

// GET /api/tickets/:id/comments
func GetComments(c fiber.Ctx) error {
	id := c.Params("id")

	var comments []models.Comment
	if err := config.DB.
		Preload("User").
		Where("ticket_id = ?", id).
		Order("created_at ASC").
		Find(&comments).Error; err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{
			"error": "Gagal mengambil komentar",
		})
	}

	return c.JSON(comments)
}

// POST /api/tickets/:id/comments
func AddComment(c fiber.Ctx) error {
	ticketID, _ := strconv.Atoi(c.Params("id"))
	userID := middleware.GetUserID(c)

	var req models.CreateCommentRequest
	if err := c.Bind().JSON(&req); err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": "Request tidak valid",
		})
	}

	if req.Content == "" {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": "Komentar tidak boleh kosong",
		})
	}

	// Pastikan tiket ada
	var ticket models.Ticket
	if err := config.DB.First(&ticket, ticketID).Error; err != nil {
		return c.Status(fiber.StatusNotFound).JSON(fiber.Map{
			"error": "Tiket tidak ditemukan",
		})
	}

	uid := uint(userID)
	comment := models.Comment{
		TicketID: uint(ticketID),
		UserID:   &uid,
		Type:     models.CommentTypeComment,
		Content:  req.Content,
	}

	if err := config.DB.Create(&comment).Error; err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{
			"error": "Gagal menambah komentar",
		})
	}

	config.DB.Preload("User").First(&comment, comment.ID)

	return c.Status(fiber.StatusCreated).JSON(comment)
}