package br.com.ordodev.qualaboa.configuracao;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.jsonwebtoken.security.Keys;

@Configuration
public class ConfiguracaoJwt {

	private static final int TAMANHO_MINIMO_EM_BYTES = 32;

	@Value("${seguranca.jwt.secret}")
	private String segredo;

	// falha no boot, de proposito: token assinado com segredo curto e falsificavel por forca bruta
	@Bean
	SecretKey chaveJwt() {
		byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
		if (bytes.length < TAMANHO_MINIMO_EM_BYTES) {
			throw new IllegalStateException(
					"JWT_SECRET ausente ou menor que 32 bytes (tem " + bytes.length + "); defina uma variavel de ambiente JWT_SECRET valida");
		}
		return Keys.hmacShaKeyFor(bytes);
	}

}
