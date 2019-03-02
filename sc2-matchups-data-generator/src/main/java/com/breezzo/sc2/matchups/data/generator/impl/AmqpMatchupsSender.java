package com.breezzo.sc2.matchups.data.generator.impl;

import com.breezzo.sc2.matchups.data.generator.MatchupsSender;
import com.breezzo.sc2.matchups.data.generator.domain.MatchupResult;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @author breezzo
 * @since 3/2/19.
 */
@Component
public class AmqpMatchupsSender implements MatchupsSender {
    @Autowired
    @Qualifier("matchupsAmqpTemplate")
    private AmqpTemplate amqpTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Override
    public void send(Collection<MatchupResult> matchups) {
        matchups.forEach(this::sendMatchup);
    }

    private void sendMatchup(MatchupResult matchup) {
        try {
            amqpTemplate.convertAndSend(objectMapper.writeValueAsBytes(matchup));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
