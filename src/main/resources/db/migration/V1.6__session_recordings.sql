CREATE TABLE IF NOT EXISTS session_recordings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id VARCHAR(255) NOT NULL,
    recording_path VARCHAR(1024) NOT NULL,
    user_id INTEGER NOT NULL,
    protocol VARCHAR(10) NOT NULL,
    recording_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
); 