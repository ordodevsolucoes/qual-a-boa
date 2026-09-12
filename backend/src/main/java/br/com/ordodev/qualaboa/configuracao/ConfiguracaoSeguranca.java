package br.com.ordodev.qualaboa.configuracao;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class ConfiguracaoSeguranca {

	@Value("${app.cors.allowed-origins}")
	private String origensPermitidas;

	// health/info precisam ficar acessiveis sem credencial para o orquestrador (Cloud Run) verificar o servico;
	// swagger-ui e api-docs ficam livres porque documentam a API e nao expoem dado de usuario
	@Bean
	SecurityFilterChain filtroDeSeguranca(HttpSecurity http, CorsConfigurationSource fonteDeConfiguracaoCors) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.cors(cors -> cors.configurationSource(fonteDeConfiguracaoCors))
			.authorizeHttpRequests(autorizacao -> autorizacao
				.requestMatchers(EndpointRequest.to("health", "info")).permitAll()
				.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
				.anyRequest().authenticated());
		return http.build();
	}

	// origens vem sempre de CORS_ORIGENS, nunca de "*", conforme restricao do projeto
	@Bean
	CorsConfigurationSource fonteDeConfiguracaoCors() {
		CorsConfiguration configuracao = new CorsConfiguration();
		configuracao.setAllowedOrigins(List.of(origensPermitidas.split(",")));
		configuracao.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuracao.setAllowedHeaders(List.of("*"));

		UrlBasedCorsConfigurationSource fonte = new UrlBasedCorsConfigurationSource();
		fonte.registerCorsConfiguration("/**", configuracao);
		return fonte;
	}

}
