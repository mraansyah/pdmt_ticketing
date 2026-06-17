package routes

import (
	"github.com/gofiber/fiber/v3"

	"pdmt-ticketing/handlers"
	"pdmt-ticketing/middleware"
)

func SetupRoutes(app *fiber.App) {
	api := app.Group("/api")

	// Auth — public (tidak perlu token)
	auth := api.Group("/auth")
	auth.Post("/login", handlers.Login)

	// Protected — semua route di bawah ini butuh token
	protected := api.Group("", middleware.Protected())

	// Auth
	protected.Get("/auth/me", handlers.GetMe)

	// Tickets
	tickets := protected.Group("/tickets")
	tickets.Get("/", handlers.GetTickets)
	tickets.Get("/:id", handlers.GetTicket)
	tickets.Post("/", handlers.CreateTicket)
	tickets.Patch("/:id", handlers.UpdateTicket)
	tickets.Delete("/:id", middleware.OnlyHO(), handlers.DeleteTicket)

	// Comments
	tickets.Get("/:id/comments", handlers.GetComments)
	tickets.Post("/:id/comments", handlers.AddComment)
}