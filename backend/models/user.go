package models

import "time"

type UserRole string

const (
	RoleAdmin      UserRole = "admin"
	RoleHeadOffice UserRole = "head_office"
	RoleSite       UserRole = "site"
)

type User struct {
	ID           uint      `gorm:"primaryKey" json:"id"`
	SiteID       *uint     `json:"site_id"`
	Site         *Site     `gorm:"foreignKey:SiteID" json:"site,omitempty"`
	Name         string    `gorm:"size:100;not null" json:"name"`
	Username     string    `gorm:"size:50;not null;uniqueIndex" json:"username"`
	Email        string    `gorm:"size:150;uniqueIndex" json:"email"`
	PasswordHash string    `gorm:"size:255;not null" json:"-"`
	Role         UserRole  `gorm:"type:user_role;default:'site'" json:"role"`
	IsActive     bool      `gorm:"default:true" json:"is_active"`
	LastLoginAt  *time.Time `json:"last_login_at"`
	CreatedAt    time.Time `json:"created_at"`
	UpdatedAt    time.Time `json:"updated_at"`
}

type UserResponse struct {
	ID        uint     `json:"id"`
	SiteID    *uint    `json:"site_id"`
	SiteName  string   `json:"site_name,omitempty"`
	Name      string   `json:"name"`
	Username  string   `json:"username"`
	Email     string   `json:"email"`
	Role      UserRole `json:"role"`
	IsActive  bool     `json:"is_active"`
}