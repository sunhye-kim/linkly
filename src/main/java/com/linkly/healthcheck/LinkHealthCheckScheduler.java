package com.linkly.healthcheck;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class LinkHealthCheckScheduler {

	private final LinkHealthCheckService linkHealthCheckService;

	@Scheduled(cron = "${health-check.schedule:0 0 2 * * *}")
	public void scheduledHealthCheck() {
		log.info("[HealthCheck] Scheduled health check started");
		linkHealthCheckService.checkAllBookmarks();
		log.info("[HealthCheck] Scheduled health check triggered (async tasks dispatched)");
	}
}
