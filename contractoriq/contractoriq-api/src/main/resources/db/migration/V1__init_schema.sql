CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE contractors (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name         VARCHAR(255) NOT NULL,
    city         VARCHAR(128) NOT NULL,
    trade        VARCHAR(32)  NOT NULL,
    phone        VARCHAR(32),
    joined_date  DATE         NOT NULL,
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT contractors_trade_chk
        CHECK (trade IN ('CIVIL','ELECTRICAL','PLUMBING','FINISHING'))
);

CREATE INDEX idx_contractors_city  ON contractors (city);
CREATE INDEX idx_contractors_trade ON contractors (trade);

CREATE TABLE projects (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contractor_id     UUID NOT NULL REFERENCES contractors(id) ON DELETE CASCADE,
    project_name      VARCHAR(255) NOT NULL,
    city              VARCHAR(128) NOT NULL,
    start_date        DATE NOT NULL,
    end_date          DATE,
    planned_end_date  DATE NOT NULL,
    budget_planned    NUMERIC(14,2) NOT NULL,
    budget_actual     NUMERIC(14,2),
    customer_rating   INTEGER,
    completion_status VARCHAR(16) NOT NULL,
    CONSTRAINT projects_status_chk
        CHECK (completion_status IN ('COMPLETED','ABANDONED','ONGOING')),
    CONSTRAINT projects_rating_chk
        CHECK (customer_rating IS NULL OR (customer_rating BETWEEN 1 AND 5))
);

CREATE INDEX idx_projects_contractor ON projects (contractor_id);
CREATE INDEX idx_projects_status     ON projects (completion_status);

CREATE TABLE site_photo_analyses (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id           UUID REFERENCES projects(id) ON DELETE SET NULL,
    contractor_id        UUID NOT NULL REFERENCES contractors(id) ON DELETE CASCADE,
    photo_path           VARCHAR(512) NOT NULL,
    claude_raw_response  JSONB,
    quality_score        INTEGER NOT NULL,
    issues_found         JSONB,
    analyzed_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT spa_quality_chk
        CHECK (quality_score BETWEEN 0 AND 100)
);

CREATE INDEX idx_spa_contractor ON site_photo_analyses (contractor_id);
CREATE INDEX idx_spa_project    ON site_photo_analyses (project_id);

CREATE TABLE contractor_scores (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contractor_id            UUID NOT NULL REFERENCES contractors(id) ON DELETE CASCADE,
    calculated_at            TIMESTAMP NOT NULL DEFAULT NOW(),
    delay_score              NUMERIC(6,2) NOT NULL,
    budget_score             NUMERIC(6,2) NOT NULL,
    quality_score            NUMERIC(6,2) NOT NULL,
    customer_score           NUMERIC(6,2) NOT NULL,
    overall_score            NUMERIC(6,2) NOT NULL,
    recommendation           VARCHAR(16)  NOT NULL,
    recommendation_reasoning TEXT,
    CONSTRAINT cs_recommendation_chk
        CHECK (recommendation IN ('HIRE','CAUTION','AVOID'))
);

CREATE INDEX idx_cs_contractor      ON contractor_scores (contractor_id);
CREATE INDEX idx_cs_overall         ON contractor_scores (overall_score DESC);
CREATE INDEX idx_cs_recommendation  ON contractor_scores (recommendation);
