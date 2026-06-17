package handlers

import (
	"os"
	"time"

	"github.com/gofiber/fiber/v3"
	"github.com/golang-jwt/jwt/v5"
	"golang.org/x/crypto/bcrypt"

	"pdmt-ticketing/config"
	"pdmt-ticketing/middleware"
	"pdmt-ticketing/models"
)

type LoginRequest struct {
	Username string `json:"username"`
	Password string `json:"password"`
}

// POST /api/auth/login
func Login(c fiber.Ctx) error {
	var req LoginRequest
	if err := c.Bind().JSON(&req); err != nil {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": "Request tidak valid",
		})
	}

	if req.Username == "" || req.Password == "" {
		return c.Status(fiber.StatusBadRequest).JSON(fiber.Map{
			"error": "Username dan password wajib diisi",
		})
	}

	// Cari user berdasarkan username
	var user models.User
	result := config.DB.Preload("Site").Where("username = ? AND is_active = true", req.Username).First(&user)
	if result.Error != nil {
		return c.Status(fiber.StatusUnauthorized).JSON(fiber.Map{
			"error": "Username atau password salah",
		})
	}

	// Cek password
	if err := bcrypt.CompareHashAndPassword([]byte(user.PasswordHash), []byte(req.Password)); err != nil {
		return c.Status(fiber.StatusUnauthorized).JSON(fiber.Map{
			"error": "Username atau password salah",
		})
	}

	// Buat JWT token
	claims := &middleware.Claims{
		UserID:   user.ID,
		Username: user.Username,
		Role:     string(user.Role),
		SiteID:   user.SiteID,
		RegisteredClaims: jwt.RegisteredClaims{
			ExpiresAt: jwt.NewNumericDate(time.Now().Add(24 * time.Hour)),
			IssuedAt:  jwt.NewNumericDate(time.Now()),
		},
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	tokenStr, err := token.SignedString([]byte(os.Getenv("JWT_SECRET")))
	if err != nil {
		return c.Status(fiber.StatusInternalServerError).JSON(fiber.Map{
			"error": "Gagal membuat token",
		})
	}

	// Update last login
	config.DB.Model(&user).Update("last_login_at", time.Now())

	// Siapkan response
	siteName := ""
	if user.Site != nil {
		siteName = user.Site.Name
	}

	return c.JSON(fiber.Map{
		"token": tokenStr,
		"user": models.UserResponse{
			ID:       user.ID,
			SiteID:   user.SiteID,
			SiteName: siteName,
			Name:     user.Name,
			Username: user.Username,
			Email:    user.Email,
			Role:     user.Role,
			IsActive: user.IsActive,
		},
	})
}

// GET /api/auth/me
func GetMe(c fiber.Ctx) error {
	userID := middleware.GetUserID(c)

	var user models.User
	if err := config.DB.Preload("Site").First(&user, userID).Error; err != nil {
		return c.Status(fiber.StatusNotFound).JSON(fiber.Map{
			"error": "User tidak ditemukan",
		})
	}

	siteName := ""
	if user.Site != nil {
		siteName = user.Site.Name
	}

	return c.JSON(models.UserResponse{
		ID:       user.ID,
		SiteID:   user.SiteID,
		SiteName: siteName,
		Name:     user.Name,
		Username: user.Username,
		Email:    user.Email,
		Role:     user.Role,
		IsActive: user.IsActive,
	})
}