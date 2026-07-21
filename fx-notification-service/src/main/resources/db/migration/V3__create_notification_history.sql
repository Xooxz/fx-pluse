CREATE TABLE notification_history
(
    notification_id    BIGSERIAL PRIMARY KEY,
    alert_condition_id BIGINT         NOT NULL,
    mbr_key            VARCHAR(26)    NOT NULL,
    symbol             VARCHAR(20)    NOT NULL,
    target_price       NUMERIC(18, 2) NOT NULL,
    current_price      NUMERIC(18, 2) NOT NULL,
    operator           VARCHAR(10)    NOT NULL,
    message            VARCHAR(500)   NOT NULL,
    read_yn            BOOLEAN        NOT NULL DEFAULT FALSE,
    read_at            TIMESTAMP,
    created_at         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);