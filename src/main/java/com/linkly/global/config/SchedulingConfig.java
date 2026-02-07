package com.linkly.global.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableScheduling
@EnableAsync
public class SchedulingConfig {

	@Value("${health-check.timeout-seconds:10}")
	private int timeoutSeconds;

	@Value("${health-check.thread-pool-size:10}")
	private int maxPoolSize;

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder
				.connectTimeout(Duration.ofSeconds(timeoutSeconds))
				.readTimeout(Duration.ofSeconds(timeoutSeconds))
				.build();
	}

	@Bean(name = "linkHealthCheckExecutor")
	public ThreadPoolTaskExecutor linkHealthCheckExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(maxPoolSize);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("link-health-");
		executor.initialize();
		return executor;
	}
}
