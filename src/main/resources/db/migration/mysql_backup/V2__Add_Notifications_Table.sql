CREATE TABLE IF NOT EXISTS notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    recipient_user_id INT,
    recipient_contact VARCHAR(255) NOT NULL,
    type ENUM('EMAIL', 'SMS', 'SYSTEM') NOT NULL,
    subject VARCHAR(255),
    message TEXT NOT NULL,
    status ENUM('PENDING', 'SENT', 'FAILED') DEFAULT 'PENDING',
    sent_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (recipient_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_notification_status ON notifications(status);
CREATE INDEX idx_notification_user ON notifications(recipient_user_id);
