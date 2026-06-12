CREATE TABLE IF NOT EXISTS cwm_wings_definition (
    wings_id TEXT PRIMARY KEY,
    item_model TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS cwm_player_wings (
    player_uuid TEXT PRIMARY KEY,
    wings_id TEXT NOT NULL,
    enabled boolean NOT NULL DEFAULT true,
    CONSTRAINT fk_wings_id
        FOREIGN KEY (wings_id)
            REFERENCES cwm_wings_definition(wings_id)
            ON DELETE CASCADE,
    CONSTRAINT unique_wings UNIQUE (player_uuid, wings_id)
);