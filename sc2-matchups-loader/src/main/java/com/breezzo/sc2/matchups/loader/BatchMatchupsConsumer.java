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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
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

    @Value("${application.consumer.batch.size}")
    private int batchSize;

    @Value("${application.consumer.batch.sleep-threads}")
    private boolean sleepThreads;

    private BlockingQueue<MatchupResult> matchupsBuffer;
    private final int threadsCount = Runtime.getRuntime().availableProcessors() / 2;
    private final Executor saveExecutor = Executors.newFixedThreadPool(threadsCount);

    @PostConstruct
    public void init() {
        matchupsBuffer = new ArrayBlockingQueue<>((int) (batchSize * 1.5));
        for (int i = 0; i < threadsCount; i++) {
            saveExecutor.execute(this::drainQueue);
        }
    }

    private void drainQueue() {
        try {
            final int sleepDelay = 500;
            long maxSleepTime = 10_000L;
            long cycleTime = 0;
            List<MatchupResult> matchups = new ArrayList<>(batchSize);
            while (true) {
                if (!sleepThreads) {
                    Stopwatch sw = Stopwatch.createStarted();
                    MatchupResult result = matchupsBuffer.take();
                    matchups.add(result);
                    matchupsBuffer.drainTo(matchups, batchSize);
                    cycleTime += sw.elapsed(TimeUnit.MILLISECONDS);
                    if (matchups.size() < batchSize && cycleTime < maxSleepTime) {
                        continue;
                    }
                } else {
                    if (matchupsBuffer.size() < batchSize && cycleTime < maxSleepTime) {
                        TimeUnit.MILLISECONDS.sleep(sleepDelay);
                        cycleTime += sleepDelay;
                    } else {
                        matchupsBuffer.drainTo(matchups, batchSize);
                    }
                }
                if (!matchups.isEmpty()) {
                    cycleTime = 0;
                    Stopwatch sw = Stopwatch.createStarted();
                    matchupRepository.saveResult(matchups);
                    int saveBatchSize = matchups.size();
                    matchups.clear();
                    sw.stop();
                    logger.debug("Saving batch duration: {}, size: {}", sw, saveBatchSize);
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
