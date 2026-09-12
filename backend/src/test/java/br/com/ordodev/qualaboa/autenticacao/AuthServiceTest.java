package br.com.ordodev.qualaboa.autenticacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.ordodev.qualaboa.excecao.CredenciaisInvalidasException;
import br.com.ordodev.qualaboa.seguranca.JwtService;
import br.com.ordodev.qualaboa.seguranca.UsuarioAutenticado;
import br.com.ordodev.qualaboa.usuario.PapelUsuario;
import br.com.ordodev.qualaboa.usuario.Usuario;
import br.com.ordodev.qualaboa.usuario.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	private static final Instant AGORA = Instant.parse("2026-09-12T12:00:00Z");

	@Mock
	private UsuarioRepository usuarioRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtService jwtService;

	private AuthService authService;
	private Usuario usuario;

	@BeforeEach
	void configurar() {
		authService = new AuthService(usuarioRepository, passwordEncoder, jwtService);
		usuario = new Usuario("local@qualaboa.dev", "hash-bcrypt", "Local Semente", PapelUsuario.LOCAL_DE_CURSO, AGORA);
		ReflectionTestUtils.setField(usuario, "id", UUID.fromString("11111111-1111-1111-1111-111111111111"));
	}

	@Test
	void autenticaComCredencialValidaEDevolveToken() {
		when(usuarioRepository.findByEmail("local@qualaboa.dev")).thenReturn(Optional.of(usuario));
		when(passwordEncoder.matches("senha-correta", "hash-bcrypt")).thenReturn(true);
		when(jwtService.gerar(any())).thenReturn("token-jwt");

		String token = authService.autenticar("local@qualaboa.dev", "senha-correta");

		assertThat(token).isEqualTo("token-jwt");
	}

	private static final String MENSAGEM_CREDENCIAIS_INVALIDAS = "Email ou senha invalidos";

	@Test
	void recusaEmailInexistenteComMensagemPadrao() {
		when(usuarioRepository.findByEmail("inexistente@qualaboa.dev")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.autenticar("inexistente@qualaboa.dev", "qualquer-senha"))
				.isInstanceOf(CredenciaisInvalidasException.class)
				.hasMessage(MENSAGEM_CREDENCIAIS_INVALIDAS);
	}

	@Test
	void recusaSenhaErradaComMesmaMensagemDoEmailInexistente() {
		when(usuarioRepository.findByEmail("local@qualaboa.dev")).thenReturn(Optional.of(usuario));
		when(passwordEncoder.matches("senha-errada", "hash-bcrypt")).thenReturn(false);

		assertThatThrownBy(() -> authService.autenticar("local@qualaboa.dev", "senha-errada"))
				.isInstanceOf(CredenciaisInvalidasException.class)
				.hasMessage(MENSAGEM_CREDENCIAIS_INVALIDAS);
	}

}
