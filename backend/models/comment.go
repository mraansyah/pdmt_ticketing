package models

import (
	"time"

	"gorm.io/datatypes"
)

type CommentType string

const (
	CommentTypeComment      CommentType = "comment"
	CommentTypeStatusChange CommentType = "status_change"
	CommentTypeAssignment   CommentType = "assignment"
	CommentTypeSystem       CommentType = "system"
)

type Comment struct {
	ID        uint           `gorm:"primaryKey" json:"id"`
	TicketID  uint           `gorm:"not null" json:"ticket_id"`
	UserID    *uint          `json:"user_id"`
	User      *User          `gorm:"foreignKey:UserID" json:"user,omitempty"`
	Type      CommentType    `gorm:"type:comment_type;default:'comment'" json:"type"`
	Content   string         `gorm:"type:text;not null" json:"content"`
	Meta      datatypes.JSON `gorm:"default:'{}'" json:"meta"`
	CreatedAt time.Time      `json:"created_at"`
}

type CreateCommentRequest struct {
	Content string `json:"content" validate:"required,min=1"`
}