package com.linkly.global.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConfigurationProperties(prefix = "ollama")
@Getter
@Setter
public class OllamaConfig {

	private String baseUrl;
	private String model;
	private int timeout;

	@Bean("ollamaRestTemplate")
	public RestTemplate ollamaRestTemplate(RestTemplateBuilder builder) {
		return builder
				.connectTimeout(Duration.ofSeconds(timeout))
				.readTimeout(Duration.ofSeconds(timeout))
				.build();
	}
}
