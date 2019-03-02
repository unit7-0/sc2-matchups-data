package com.breezzo.sc2.matchups.loader;

import com.breezzo.sc2.matchups.loader.repository.impl.elasticsearch.MatchupResultElasticSearchRepository;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication
@EnableRabbit
@EnableElasticsearchRepositories(basePackageClasses = MatchupResultElasticSearchRepository.class)
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
