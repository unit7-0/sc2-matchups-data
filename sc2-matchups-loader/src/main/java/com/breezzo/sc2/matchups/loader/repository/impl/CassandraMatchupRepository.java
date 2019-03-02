package com.breezzo.sc2.matchups.loader.repository.impl;

import com.breezzo.sc2.matchups.loader.domain.MatchupResult;
import com.breezzo.sc2.matchups.loader.repository.MatchupRepository;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.querybuilder.Batch;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author breezzo
 * @since 3/2/19.
 */
@Repository
@Profile("cassandra")
public class CassandraMatchupRepository implements MatchupRepository {

    private static final Logger logger = LoggerFactory.getLogger(CassandraMatchupRepository.class);

    private static final String INSERT_CQL= "INSERT INTO matchup_result(" +
            " id, matchup_id, started_at, duration_ms, gamer_race, gamer_username, gamer_win, map_name)" +
            " VALUES (:id, :matchup_id, :started_at, :duration_ms, :gamer_race, :gamer_username, :gamer_win, :map_name)";

    private static final String BATCH_INSERT_CQL= "INSERT INTO matchup_result(" +
            " id, matchup_id, started_at, duration_ms, gamer_race, gamer_username, gamer_win, map_name)" +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final int BATCH_SIZE = 150;

    @Autowired
    private Cluster cassandraCluster;

    private Session session;

    @PostConstruct
    public void init() {
        session = cassandraCluster.connect("sc2");
    }

    @Override
    public void saveResult(MatchupResult matchupResult) {
        for (MatchupResult.GamerInfo gamer : matchupResult.getGamers()) {
            ResultSet result = session.execute(INSERT_CQL, toMap(matchupResult, gamer));
            logger.debug("Row insert result: {}", result);
        }
    }

    private Map<String, Object> toMap(MatchupResult matchupResult, MatchupResult.GamerInfo gamerInfo) {
        return Map.of(
                "id", nextId(),
                "matchup_id", matchupResult.getMatchupId(),
                "started_at", Timestamp.from(matchupResult.getStartedAt()),
                "duration_ms", matchupResult.getDurationMs(),
                "gamer_race", gamerInfo.getRace(),
                "gamer_username", gamerInfo.getUsername(),
                "gamer_win", gamerInfo.isWin(),
                "map_name", matchupResult.getMapName()
        );
    }

    private String nextId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public void saveResult(List<MatchupResult> matchups) {
        if (matchups.isEmpty()) {
            return;
        }

        int startIndex = 0;
        while (startIndex < matchups.size()) {
            List<MatchupResult> batchList = matchups.subList(startIndex, Math.min(matchups.size(), startIndex + BATCH_SIZE));
            batchInsert(batchList);
            startIndex += BATCH_SIZE;
        }
    }

    private void batchInsert(List<MatchupResult> matchups) {
        List<SimpleStatement> statements = new ArrayList<>(matchups.size() * 2);
        for (MatchupResult matchupResult : matchups) {
            for (MatchupResult.GamerInfo gamer : matchupResult.getGamers()) {
                SimpleStatement statement = new SimpleStatement(
                        BATCH_INSERT_CQL,
                        nextId(),
                        matchupResult.getMatchupId(),
                        Timestamp.from(matchupResult.getStartedAt()),
                        matchupResult.getDurationMs(),
                        gamer.getRace(),
                        gamer.getUsername(),
                        gamer.isWin(),
                        matchupResult.getMapName()
                );
                statements.add(statement);
            }
        }
        SimpleStatement[] statementsArray = new SimpleStatement[statements.size()];
        statementsArray = statements.toArray(statementsArray);
        Batch batch = QueryBuilder.batch(statementsArray);
        ResultSet result = session.execute(batch);
        logger.debug("Batch insert result: {}", result);
    }
}
