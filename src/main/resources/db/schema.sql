-- Таблица для хранения пользователей
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    mac_address TEXT NOT NULL UNIQUE,
    vk_user_id INTEGER,
    is_subscribed INTEGER DEFAULT 0,
    last_check_time INTEGER,
    first_seen_time INTEGER NOT NULL,
    last_seen_time INTEGER NOT NULL,
    device_name TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Таблица для логирования доступа
CREATE TABLE IF NOT EXISTS access_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    mac_address TEXT NOT NULL,
    action TEXT NOT NULL, -- 'allowed', 'blocked', 'notified'
    ip_address TEXT,
    requested_url TEXT,
    reason TEXT,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- Таблица для отправленных уведомлений
CREATE TABLE IF NOT EXISTS notifications (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    vk_user_id INTEGER,
    mac_address TEXT NOT NULL,
    message_type TEXT, -- 'subscription_request', 'welcome', 'reminder'
    vk_message_id INTEGER,
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_read INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users (id)
);

-- Таблица для кеширования статуса подписки
CREATE TABLE IF NOT EXISTS subscription_cache (
    vk_user_id INTEGER PRIMARY KEY,
    is_subscribed INTEGER NOT NULL,
    cached_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME
);

-- Таблица для настроек и параметров
CREATE TABLE IF NOT EXISTS settings (
    setting_key TEXT PRIMARY KEY,
    setting_value TEXT NOT NULL,
    description TEXT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Индексы для оптимизации
CREATE INDEX IF NOT EXISTS idx_mac_address ON users (mac_address);
CREATE INDEX IF NOT EXISTS idx_vk_user_id ON users (vk_user_id);
CREATE INDEX IF NOT EXISTS idx_is_subscribed ON users (is_subscribed);
CREATE INDEX IF NOT EXISTS idx_access_logs_user_id ON access_logs (user_id);
CREATE INDEX IF NOT EXISTS idx_access_logs_timestamp ON access_logs (timestamp);
CREATE INDEX IF NOT EXISTS idx_notifications_vk_user_id ON notifications (vk_user_id);

