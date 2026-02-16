-- ============================================
-- User Accounts Table
-- ============================================
CREATE TABLE IF NOT EXISTS user_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email_verified BOOLEAN DEFAULT FALSE,
    verify_token VARCHAR(128),
    verify_token_expires BIGINT,
    reset_token VARCHAR(128),
    reset_token_expires BIGINT,
    -- Virtual device credentials (auto-generated at registration)
    device_id VARCHAR(64) NOT NULL UNIQUE,
    device_secret VARCHAR(128) NOT NULL,
    -- Subscription
    subscription_status VARCHAR(32) DEFAULT 'free',
    subscription_provider VARCHAR(32),
    subscription_expires_at BIGINT,
    tappay_card_key TEXT,
    tappay_card_token TEXT,
    -- Preferences
    language VARCHAR(10) DEFAULT 'en',
    -- Metadata
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    last_login_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX IF NOT EXISTS idx_user_accounts_email ON user_accounts(email);
CREATE INDEX IF NOT EXISTS idx_user_accounts_device_id ON user_accounts(device_id);
CREATE INDEX IF NOT EXISTS idx_user_accounts_verify_token ON user_accounts(verify_token);
CREATE INDEX IF NOT EXISTS idx_user_accounts_reset_token ON user_accounts(reset_token);

-- ============================================
-- Server-side Usage Tracking
-- ============================================
CREATE TABLE IF NOT EXISTS usage_tracking (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id VARCHAR(64) NOT NULL,
    date DATE NOT NULL DEFAULT CURRENT_DATE,
    message_count INTEGER DEFAULT 0,
    UNIQUE(device_id, date)
);

CREATE INDEX IF NOT EXISTS idx_usage_device_date ON usage_tracking(device_id, date);

-- ============================================
-- TapPay Transaction Log
-- ============================================
CREATE TABLE IF NOT EXISTS tappay_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_account_id UUID NOT NULL REFERENCES user_accounts(id),
    rec_trade_id VARCHAR(255),
    amount INTEGER NOT NULL,
    currency VARCHAR(10) DEFAULT 'TWD',
    status VARCHAR(32) NOT NULL,
    tappay_response JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_tappay_user ON tappay_transactions(user_account_id);

-- ============================================
-- Chat Message History (server-side persistence)
-- ============================================
CREATE TABLE IF NOT EXISTS chat_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    device_id VARCHAR(64) NOT NULL,
    entity_id INTEGER,
    text TEXT NOT NULL,
    source VARCHAR(64) NOT NULL,
    is_from_user BOOLEAN DEFAULT FALSE,
    is_from_bot BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_chat_device ON chat_messages(device_id, created_at DESC);
