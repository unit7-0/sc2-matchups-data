package com.breezzo.sc2.matchups.data.generator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@RequestMapping("/api/sc2")
public class Application {

	@Autowired
	private MatchupsDataGenerator matchupsDataGenerator;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@GetMapping("/data/generate")
	public void runGeneration(@RequestParam int matchupsCount) {
		matchupsDataGenerator.generateAndUpload(matchupsCount);
	}
}
