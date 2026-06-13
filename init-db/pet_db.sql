\connect pet_db

CREATE TABLE IF NOT EXISTS match_results (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lost_notice_id UUID NOT NULL,
    seen_notice_id UUID NOT NULL,
    lost_owner_id UUID NOT NULL,
    similarity_score DOUBLE PRECISION NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
    );
