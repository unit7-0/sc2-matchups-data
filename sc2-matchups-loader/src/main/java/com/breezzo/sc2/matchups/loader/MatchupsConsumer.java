package com.breezzo.sc2.matchups.loader;

import com.breezzo.sc2.matchups.loader.domain.MatchupResult;
import com.breezzo.sc2.matchups.loader.repository.MatchupRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author breezzo
 * @since 3/1/19.
 */
@Component
public class MatchupsConsumer {
    private static final Logger logger = LoggerFactory.getLogger(MatchupsConsumer.class);

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private MatchupRepository matchupRepository;

    @RabbitListener(queues = "${application.mq.matchups-queue-name}", concurrency = "8")
    public void onMessage(Message message) {
        try {
            handleMessage(message);
        } catch (Exception e) {
            logger.error("Exception while handling message: {}", message, e);
        }
    }

    private void handleMessage(Message message) throws IOException {
        Stopwatch sw = Stopwatch.createStarted();
        logger.trace("Incoming message: {}", message);
        byte[] body = message.getBody();
        MatchupResult matchupResult = objectMapper.readValue(body, MatchupResult.class);
        Stopwatch saveSw= Stopwatch.createStarted();
        matchupRepository.saveResult(matchupResult);
        sw.stop();
        saveSw.stop();
        logger.debug("Message handling duration: {}, save duration: {}", sw, saveSw);
    }
}
