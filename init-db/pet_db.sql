CREATE SCHEMA IF NOT EXISTS pet_db;

CREATE TABLE IF NOT EXISTS pet_db.match_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lost_notice_id UUID NOT NULL,
    seen_notice_id UUID NOT NULL,
    similarity_score DOUBLE PRECISION NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
    );

CREATE TABLE IF NOT EXISTS pet_db.notice_embeddings (
    notice_id UUID PRIMARY KEY,
    embedding DOUBLE PRECISION[] NOT NULL
);