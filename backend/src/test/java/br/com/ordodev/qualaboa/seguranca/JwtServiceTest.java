package br.com.ordodev.qualaboa.seguranca;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import br.com.ordodev.qualaboa.usuario.PapelUsuario;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

	private static final Instant AGORA = Instant.parse("2026-09-12T12:00:00Z");
	private static final UUID USUARIO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

	private JwtService jwtService;

	@BeforeEach
	void configurar() {
		SecretKey chave = Keys.hmacShaKeyFor("a".repeat(32).getBytes(StandardCharsets.UTF_8));
		Clock clock = Clock.fixed(AGORA, ZoneOffset.UTC);
		jwtService = new JwtService(chave, clock);
	}

	@Test
	void geraTokenComIdNoSubjectEPapelComoClaim() {
		String token = jwtService.gerar(new UsuarioAutenticado(USUARIO_ID, PapelUsuario.LOCAL_DE_CURSO));

		UsuarioAutenticado usuarioAutenticado = jwtService.validar(token);

		assertThat(usuarioAutenticado.id()).isEqualTo(USUARIO_ID);
		assertThat(usuarioAutenticado.papel()).isEqualTo(PapelUsuario.LOCAL_DE_CURSO);
	}

	@Test
	void recusaTokenExpirado() {
		String token = jwtService.gerar(new UsuarioAutenticado(USUARIO_ID, PapelUsuario.PARTICIPANTE));
		JwtService jwtServiceNoFuturo = new JwtService(chaveDoTeste(), Clock.fixed(AGORA.plusSeconds(31 * 60), ZoneOffset.UTC));

		assertThatThrownBy(() -> jwtServiceNoFuturo.validar(token)).isInstanceOf(JwtException.class);
	}

	@Test
	void recusaTokenComAssinaturaInvalida() {
		String token = jwtService.gerar(new UsuarioAutenticado(USUARIO_ID, PapelUsuario.PARTICIPANTE));
		SecretKey outraChave = Keys.hmacShaKeyFor("b".repeat(32).getBytes(StandardCharsets.UTF_8));
		JwtService jwtServiceComOutraChave = new JwtService(outraChave, Clock.fixed(AGORA, ZoneOffset.UTC));

		assertThatThrownBy(() -> jwtServiceComOutraChave.validar(token)).isInstanceOf(JwtException.class);
	}

	private SecretKey chaveDoTeste() {
		return Keys.hmacShaKeyFor("a".repeat(32).getBytes(StandardCharsets.UTF_8));
	}

}
