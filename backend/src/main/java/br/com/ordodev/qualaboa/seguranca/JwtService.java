package br.com.ordodev.qualaboa.seguranca;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import br.com.ordodev.qualaboa.usuario.PapelUsuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;

@Service
public class JwtService {

	private static final Duration EXPIRACAO = Duration.ofMinutes(30);
	private static final String CLAIM_PAPEL = "papel";

	private final SecretKey chave;
	private final Clock clock;

	public JwtService(SecretKey chaveJwt, Clock clock) {
		this.chave = chaveJwt;
		this.clock = clock;
	}

	public String gerar(UsuarioAutenticado usuarioAutenticado) {
		Instant agora = clock.instant();
		return Jwts.builder()
				.subject(usuarioAutenticado.id().toString())
				.claim(CLAIM_PAPEL, usuarioAutenticado.papel().name())
				.issuedAt(Date.from(agora))
				.expiration(Date.from(agora.plus(EXPIRACAO)))
				.signWith(chave, Jwts.SIG.HS256)
				.compact();
	}

	// lanca io.jsonwebtoken.JwtException (assinatura invalida, token expirado, etc.) para o chamador decidir;
	// nunca loga o token nem o motivo detalhado, para nao vazar segredo de autenticacao em log
	public UsuarioAutenticado validar(String token) {
		// clock explicito: sem isso o parser valida "exp" contra o relogio real da maquina,
		// quebrando o teste com Clock fixo e violando a convencao de instante via Clock injetado
		Jws<Claims> jws = Jwts.parser().clock(() -> Date.from(clock.instant())).verifyWith(chave).build().parseSignedClaims(token);
		Claims claims = jws.getPayload();
		UUID id = UUID.fromString(claims.getSubject());
		PapelUsuario papel = PapelUsuario.valueOf(claims.get(CLAIM_PAPEL, String.class));
		return new UsuarioAutenticado(id, papel);
	}

}
