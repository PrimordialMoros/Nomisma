-- Nomisma PostgreSQL Schema

CREATE TABLE IF NOT EXISTS nomisma_players (
    player_uuid     UUID PRIMARY KEY        NOT NULL,
    player_name     VARCHAR(17)             NOT NULL
);
CREATE INDEX IF NOT EXISTS player_name_index ON nomisma_players (player_name);
