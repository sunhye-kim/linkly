package com.linkly.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI linklyOpenAPI() {
		return new OpenAPI()
				.info(new Info().title("Linkly API").description("북마크 관리 서비스 API 문서").version("v0.0.1")
						.contact(new Contact().name("Linkly Team").email("support@linkly.com")))
				.servers(List.of(new Server().url("http://localhost:8080").description("로컬 개발 서버"),
						new Server().url("https://api.linkly.com").description("프로덕션 서버")));
	}
}
