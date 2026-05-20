CREATE TABLE urls (
    id           BIGSERIAL PRIMARY KEY,
    short_code   VARCHAR(10)  NOT NULL UNIQUE,
    original_url TEXT         NOT NULL,
    access_count BIGINT       NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    expired_at   TIMESTAMPTZ
);

CREATE INDEX idx_urls_short_code   ON urls (short_code);
CREATE INDEX idx_urls_access_count ON urls (access_count DESC);
CREATE INDEX idx_urls_expired_at   ON urls (expired_at) WHERE expired_at IS NOT NULL;
