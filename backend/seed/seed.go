package main

import (
	"fmt"
	"log"

	"github.com/joho/godotenv"
	"golang.org/x/crypto/bcrypt"

	"pdmt-ticketing/config"
	"pdmt-ticketing/models"
)

func main() {
	if err := godotenv.Load(); err != nil {
		log.Fatal("Error loading .env file")
	}

	config.ConnectDB()

	// Hash password
	hashPassword := func(password string) string {
		hash, err := bcrypt.GenerateFromPassword([]byte(password), bcrypt.DefaultCost)
		if err != nil {
			log.Fatal("Gagal hash password:", err)
		}
		return string(hash)
	}

	// Update password hash untuk semua user
	users := []struct {
		Username string
		Password string
	}{
		{"admin", "admin123"},
		{"ho_team", "ho123"},
		{"budi_kl", "budi123"},
		{"andi_pg", "andi123"},
	}

	for _, u := range users {
		hash := hashPassword(u.Password)
		result := config.DB.Model(&models.User{}).
			Where("username = ?", u.Username).
			Update("password_hash", hash)

		if result.Error != nil {
			log.Printf("Gagal update user %s: %v", u.Username, result.Error)
		} else if result.RowsAffected == 0 {
			log.Printf("User %s tidak ditemukan", u.Username)
		} else {
			fmt.Printf("✓ Password user '%s' berhasil di-set\n", u.Username)
		}
	}

	fmt.Println("\nSeed selesai! Silakan login dengan:")
	fmt.Println("  admin    / admin123")
	fmt.Println("  ho_team  / ho123")
	fmt.Println("  budi_kl  / budi123")
	fmt.Println("  andi_pg  / andi123")
}