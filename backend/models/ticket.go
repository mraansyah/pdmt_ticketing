package models

import "time"

type TicketStatus string
type TicketPriority string

const (
	StatusOpen       TicketStatus = "open"
	StatusOnProgress TicketStatus = "on_progress"
	StatusResolved   TicketStatus = "resolved"
	StatusClosed     TicketStatus = "closed"
)

const (
	PriorityLow      TicketPriority = "low"
	PriorityMedium   TicketPriority = "medium"
	PriorityHigh     TicketPriority = "high"
	PriorityCritical TicketPriority = "critical"
)

type Ticket struct {
	ID           uint           `gorm:"primaryKey" json:"id"`
	TicketNumber string         `gorm:"size:20;not null;uniqueIndex" json:"ticket_number"`
	SiteID       uint           `gorm:"not null" json:"site_id"`
	Site         Site           `gorm:"foreignKey:SiteID" json:"site,omitempty"`
	CategoryID   *uint          `json:"category_id"`
	Category     *Category      `gorm:"foreignKey:CategoryID" json:"category,omitempty"`
	CreatedBy    uint           `gorm:"not null" json:"created_by"`
	Creator      User           `gorm:"foreignKey:CreatedBy" json:"creator,omitempty"`
	AssignedTo   *uint          `json:"assigned_to"`
	Assignee     *User          `gorm:"foreignKey:AssignedTo" json:"assignee,omitempty"`
	Title        string         `gorm:"size:255;not null" json:"title"`
	Description  string         `gorm:"type:text;not null" json:"description"`
	Status       TicketStatus   `gorm:"type:ticket_status;default:'open'" json:"status"`
	Priority     TicketPriority `gorm:"type:ticket_priority;default:'medium'" json:"priority"`
	ResolvedAt   *time.Time     `json:"resolved_at"`
	ClosedAt     *time.Time     `json:"closed_at"`
	CreatedAt    time.Time      `json:"created_at"`
	UpdatedAt    time.Time      `json:"updated_at"`
	Comments     []Comment      `gorm:"foreignKey:TicketID" json:"comments,omitempty"`
}

type Category struct {
	ID       uint   `gorm:"primaryKey" json:"id"`
	Name     string `gorm:"size:100;not null;uniqueIndex" json:"name"`
	Color    string `gorm:"size:7;default:'#6B7280'" json:"color"`
	IsActive bool   `gorm:"default:true" json:"is_active"`
}

// Request body untuk buat tiket baru
type CreateTicketRequest struct {
	CategoryID  *uint          `json:"category_id"`
	AssignedTo  *uint          `json:"assigned_to"`
	Title       string         `json:"title" validate:"required,min=5"`
	Description string         `json:"description" validate:"required,min=10"`
	Priority    TicketPriority `json:"priority"`
}

// Request body untuk update status tiket
type UpdateTicketRequest struct {
	Status     TicketStatus `json:"status"`
	AssignedTo *uint        `json:"assigned_to"`
	CategoryID *uint        `json:"category_id"`
	Priority   TicketPriority `json:"priority"`
}