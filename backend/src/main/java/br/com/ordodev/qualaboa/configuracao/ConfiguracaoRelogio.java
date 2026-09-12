package br.com.ordodev.qualaboa.configuracao;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracaoRelogio {

	// instante atual sempre via Clock injetado, nunca LocalDateTime.now() direto, para os testes de data serem deterministicos
	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

}
