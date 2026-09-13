package br.com.ordodev.qualaboa.configuracao;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import br.com.ordodev.qualaboa.seguranca.JwtAuthenticationFilter;
import br.com.ordodev.qualaboa.seguranca.JwtService;

@Configuration
public class ConfiguracaoSeguranca {

	private static final int CUSTO_BCRYPT = 10;

	@Value("${app.cors.allowed-origins}")
	private String origensPermitidas;

	// health/info e login ficam livres para o cliente conseguir autenticar; swagger-ui e
	// api-docs tambem, porque documentam a API e nao expoem dado de usuario. API stateless:
	// nenhuma sessao HTTP, o estado de autenticacao vem inteiro do JWT em cada requisicao
	@Bean
	SecurityFilterChain filtroDeSeguranca(HttpSecurity http, CorsConfigurationSource fonteDeConfiguracaoCors,
			JwtService jwtService) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.cors(cors -> cors.configurationSource(fonteDeConfiguracaoCors))
			.sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(autorizacao -> autorizacao
				.requestMatchers(EndpointRequest.to("health", "info")).permitAll()
				.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
				.requestMatchers("/api/v1/auth/login").permitAll()
				.anyRequest().authenticated())
			.addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
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

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(CUSTO_BCRYPT);
	}

	// desliga o usuario em memoria com senha gerada que o Spring Boot cria por padrao
	// (e loga a senha no console); a autenticacao real e via JWT, nao via UserDetailsService
	@Bean
	UserDetailsService usuariosEmMemoriaVazio() {
		return new InMemoryUserDetailsManager();
	}

}
