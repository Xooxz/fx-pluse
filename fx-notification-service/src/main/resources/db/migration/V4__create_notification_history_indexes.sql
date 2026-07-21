CREATE INDEX idx_notification_history_mbr_created_at
    ON notification_history (mbr_key, created_at DESC);

CREATE INDEX idx_notification_history_mbr_read_yn
    ON notification_history (mbr_key, read_yn);