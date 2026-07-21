CREATE TABLE alert_condition
(
    alert_condition_id BIGSERIAL PRIMARY KEY,
    mbr_key            VARCHAR(26)    NOT NULL,
    alert_seq          BIGINT         NOT NULL,
    symbol             VARCHAR(20)    NOT NULL,
    target_price       NUMERIC(18, 2) NOT NULL,
    operator           VARCHAR(10)    NOT NULL,
    interval           VARCHAR(30)    NOT NULL,
    last_sent_at       TIMESTAMP,
    use_yn             BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by         VARCHAR(26)    NOT NULL,
    modified_at        TIMESTAMP,
    modified_by        VARCHAR(26),
    CONSTRAINT uk_alert_condition_mbr_seq
        UNIQUE (mbr_key, alert_seq)
);