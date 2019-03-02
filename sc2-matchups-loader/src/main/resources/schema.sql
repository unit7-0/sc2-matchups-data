CREATE TABLE sc2.matchup_result
(
    started_at DateTime,
    matchup_id UInt64,
    map_name String,
    duration_ms UInt64,
    gamer_username String,
    gamer_race String,
    gamer_win UInt8
)
ENGINE = MergeTree()
PARTITION BY toYYYYMM(started_at)
ORDER BY (started_at, matchup_id);

CREATE TABLE sc2.matchups_buffer AS sc2.matchup_result ENGINE = Buffer(sc2, matchup_result, 16, 30, 300, 10000, 1000000, 5000000, 50000000);