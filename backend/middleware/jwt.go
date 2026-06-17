package middleware

import (
	"os"
	"strings"

	"github.com/gofiber/fiber/v3"
	"github.com/golang-jwt/jwt/v5"
)

type Claims struct {
	UserID   uint   `json:"user_id"`
	Username string `json:"username"`
	Role     string `json:"role"`
	SiteID   *uint  `json:"site_id"`
	jwt.RegisteredClaims
}

func Protected() fiber.Handler {
	return func(c fiber.Ctx) error {
		authHeader := c.Get("Authorization")
		if authHeader == "" {
			return c.Status(fiber.StatusUnauthorized).JSON(fiber.Map{
				"error": "Token tidak ditemukan",
			})
		}

		tokenStr := strings.TrimPrefix(authHeader, "Bearer ")
		if tokenStr == authHeader {
			return c.Status(fiber.StatusUnauthorized).JSON(fiber.Map{
				"error": "Format token tidak valid, gunakan Bearer <token>",
			})
		}

		claims := &Claims{}
		token, err := jwt.ParseWithClaims(tokenStr, claims, func(t *jwt.Token) (interface{}, error) {
			return []byte(os.Getenv("JWT_SECRET")), nil
		})

		if err != nil || !token.Valid {
			return c.Status(fiber.StatusUnauthorized).JSON(fiber.Map{
				"error": "Token tidak valid atau sudah expired",
			})
		}

		// Simpan claims ke context supaya bisa diakses di handler
		c.Locals("userID", claims.UserID)
		c.Locals("username", claims.Username)
		c.Locals("role", claims.Role)
		c.Locals("siteID", claims.SiteID)

		return c.Next()
	}
}

// Helper: ambil userID dari context
func GetUserID(c fiber.Ctx) uint {
	return c.Locals("userID").(uint)
}

// Helper: ambil role dari context
func GetRole(c fiber.Ctx) string {
	return c.Locals("role").(string)
}

// Helper: ambil siteID dari context
func GetSiteID(c fiber.Ctx) *uint {
	siteID, ok := c.Locals("siteID").(*uint)
	if !ok {
		return nil
	}
	return siteID
}

func OnlyHO() fiber.Handler {
	return func(c fiber.Ctx) error {
		role := GetRole(c)
		if role != "head_office" && role != "admin" {
			return c.Status(fiber.StatusForbidden).JSON(fiber.Map{
				"error": "Akses ditolak, hanya Head Office yang diizinkan",
			})
		}
		return c.Next()
	}
}