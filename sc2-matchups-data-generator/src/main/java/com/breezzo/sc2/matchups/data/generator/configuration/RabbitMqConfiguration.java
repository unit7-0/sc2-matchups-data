package com.breezzo.sc2.matchups.data.generator.configuration;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author breezzo
 * @since 3/1/19.
 */
@Configuration
public class RabbitMqConfiguration {

    @Value("${application.mq.matchups-exchange-name}")
    private String matchupsExchange;

    @Bean
    public AmqpTemplate matchupsAmqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setChannelTransacted(false);
        rabbitTemplate.setExchange(matchupsExchange);
        return rabbitTemplate;
    }
}
