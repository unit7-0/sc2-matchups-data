CREATE KEYSPACE sc2 WITH REPLICATION = {
  'class': 'SimpleStrategy',
  'replication_factor': '3'
  };
;

CREATE TABLE sc2.matchup_result(
   id text,
   started_at timestamp,
   matchup_id bigint,
   map_name ascii,
   duration_ms bigint,
   gamer_username ascii,
   gamer_race ascii,
   gamer_win boolean,
   primary key (id, started_at)
)
with clustering order by (started_at asc);
