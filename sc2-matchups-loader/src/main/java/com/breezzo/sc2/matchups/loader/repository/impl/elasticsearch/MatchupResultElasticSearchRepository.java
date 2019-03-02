package com.breezzo.sc2.matchups.loader.repository.impl.elasticsearch;

import com.breezzo.sc2.matchups.loader.domain.MatchupResultDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author breezzo
 * @since 3/2/19.
 */
@Repository
public interface MatchupResultElasticSearchRepository extends ElasticsearchRepository<MatchupResultDocument, Long> {
}
