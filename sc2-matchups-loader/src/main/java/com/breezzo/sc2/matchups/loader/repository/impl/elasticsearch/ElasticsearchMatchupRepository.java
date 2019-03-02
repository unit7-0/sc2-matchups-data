package com.breezzo.sc2.matchups.loader.repository.impl.elasticsearch;

import com.breezzo.sc2.matchups.loader.domain.MatchupResult;
import com.breezzo.sc2.matchups.loader.domain.MatchupResultDocument;
import com.breezzo.sc2.matchups.loader.repository.MatchupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author breezzo
 * @since 3/2/19.
 */
@Repository
@Profile("elasticsearch")
public class ElasticsearchMatchupRepository implements MatchupRepository {
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchMatchupRepository.class);

    @Autowired
    private MatchupResultElasticSearchRepository matchupResultElasticSearchRepository;

    @Override
    public void saveResult(MatchupResult matchupResult) {
        MatchupResultDocument document = matchupResultElasticSearchRepository.index(toDocument(matchupResult));
        logger.debug("Saved document: {}", document);
    }

    private MatchupResultDocument toDocument(MatchupResult result) {
        MatchupResultDocument document = new MatchupResultDocument();
        document.setId(result.getMatchupId());
        document.setDurationMs(result.getDurationMs());
        document.setStartedAt(Timestamp.from(result.getStartedAt()));
        document.setMapName(result.getMapName());

        List<MatchupResultDocument.GamerInfo> gamerDocuments = new ArrayList<>();
        for (MatchupResult.GamerInfo gamer : result.getGamers()) {
            MatchupResultDocument.GamerInfo gamerDocument = new MatchupResultDocument.GamerInfo();
            gamerDocument.setRace(gamer.getRace());
            gamerDocument.setUsername(gamer.getUsername());
            gamerDocument.setWin(gamer.isWin());
            gamerDocuments.add(gamerDocument);
        }
        document.setGamers(gamerDocuments);
        return document;
    }

    @Override
    public void saveResult(List<MatchupResult> matchups) {
        if (matchups.isEmpty()) {
            return;
        }
        List<MatchupResultDocument> documents = matchups.stream()
                .map(this::toDocument)
                .collect(Collectors.toList());
        matchupResultElasticSearchRepository.saveAll(documents);
    }
}
