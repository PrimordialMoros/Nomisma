-- Nomisma MariaDB Schema

CREATE TABLE IF NOT EXISTS nomisma_players (
    player_uuid     BINARY(16)              NOT NULL,
    player_name     VARCHAR(16)             NOT NULL,
    PRIMARY KEY (player_uuid)
) DEFAULT CHARSET = utf8mb4;
CREATE INDEX IF NOT EXISTS player_name_index ON nomisma_players (player_name);
