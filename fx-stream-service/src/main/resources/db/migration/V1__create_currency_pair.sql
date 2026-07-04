CREATE TABLE currency_pair
(
    id           BIGSERIAL PRIMARY KEY,
    symbol       VARCHAR(20)  NOT NULL UNIQUE,
    country_name VARCHAR(50)  NOT NULL,
    enabled      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);