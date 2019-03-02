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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author breezzo
 * @since 3/1/19.
 */
@Component
@ConditionalOnProperty("application.consumer.batch.enabled")
public class BatchMatchupsConsumer {
    private static final Logger logger = LoggerFactory.getLogger(BatchMatchupsConsumer.class);

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    private MatchupRepository matchupRepository;

    private final int batchSize = 5000;
    private BlockingQueue<MatchupResult> matchupsBuffer = new ArrayBlockingQueue<>(30_000);
    private final int threadsCount = Runtime.getRuntime().availableProcessors();
    private final Executor saveExecutor = Executors.newFixedThreadPool(threadsCount);

    @PostConstruct
    public void init() {
        for (int i = 0; i < threadsCount; i++) {
            saveExecutor.execute(this::drainQueue);
        }
    }

    private void drainQueue() {
        try {
            List<MatchupResult> matchups = new ArrayList<>(batchSize);
            while (true) {
                if (matchupsBuffer.size() < batchSize) {
                    TimeUnit.MILLISECONDS.sleep(500);
                } else {
                    Stopwatch sw = Stopwatch.createStarted();
                    matchupsBuffer.drainTo(matchups, batchSize);
                    matchupRepository.saveResult(matchups);
                    matchups.clear();
                    sw.stop();
                    logger.debug("Saving batch duration: {}", sw);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }
    }

    @RabbitListener(queues = "${application.mq.matchups-queue-name}", concurrency = "8")
    public void onMessage(Message message) {
        try {
            enqueueMessage(message);
        } catch (Exception e) {
            logger.error("Exception while handling message: {}", message, e);
        }
    }

    private void enqueueMessage(Message message) throws IOException, InterruptedException {
        Stopwatch sw = Stopwatch.createStarted();
        logger.trace("Incoming message: {}", message);
        byte[] body = message.getBody();
        MatchupResult matchupResult = objectMapper.readValue(body, MatchupResult.class);
        matchupsBuffer.put(matchupResult);
        sw.stop();
        logger.debug("Message queuing duration: {}", sw);
    }
}
