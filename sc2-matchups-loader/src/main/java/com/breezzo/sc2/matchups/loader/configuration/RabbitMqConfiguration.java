package com.breezzo.sc2.matchups.loader.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author breezzo
 * @since 3/1/19.
 */
@Configuration
public class RabbitMqConfiguration {

    @Value("${application.mq.matchups-queue-name}")
    private String matchupsQueueName;

    @Value("${application.mq.matchups-exchange-name}")
    private String matchupsExchangeName;

    @Bean
    public Queue matchupsQueue() {
        return new Queue(matchupsQueueName, true, false, false);
    }

    @Bean
    public FanoutExchange matchupsExchange() {
        return new FanoutExchange(matchupsExchangeName, true, false);
    }

    @Bean
    public Binding matchupsExchangeToQueue() {
        return BindingBuilder.bind(matchupsQueue()).to(matchupsExchange());
    }
}
