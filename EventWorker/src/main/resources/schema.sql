CREATE TABLE IF NOT EXISTS campaigns (
    id BIGSERIAL PRIMARY KEY,
    campaign_id VARCHAR(255) NOT NULL,
    sub_campaign_id VARCHAR(255)
);

CREATE UNIQUE INDEX campaign_business_key
ON campaigns (campaign_id, sub_campaign_id) NULLS NOT DISTINCT;

CREATE TABLE IF NOT EXISTS attendees (
    id BIGSERIAL PRIMARY KEY,
    cn VARCHAR(255),
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    birth_date DATE,
    partner_id VARCHAR(255) NOT NULL,
    is_companion BOOLEAN NOT NULL,
    qr_code VARCHAR(255) NOT NULL UNIQUE,
    campaign_id_rif BIGINT NOT NULL,
    CONSTRAINT fk_campaign
        FOREIGN KEY (campaign_id_rif) REFERENCES campaigns(id)
        ON DELETE CASCADE
);


