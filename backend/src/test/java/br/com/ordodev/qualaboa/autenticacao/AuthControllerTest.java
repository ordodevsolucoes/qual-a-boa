package br.com.ordodev.qualaboa.autenticacao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.ordodev.qualaboa.excecao.CredenciaisInvalidasException;

// filtros de seguranca desligados de proposito: aqui o alvo e o contrato do controller
// (delega para AuthService, formata a resposta), nao a politica de autorizacao, que e
// testada a parte quando o filtro JWT de verdade existir
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private AuthService authService;

	@Test
	void loginComCredencialValidaDevolveToken() throws Exception {
		when(authService.autenticar("local@qualaboa.dev", "senha-correta")).thenReturn("token-jwt");

		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{ "email": "local@qualaboa.dev", "senha": "senha-correta" }
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("token-jwt"));
	}

	@Test
	void loginComEmailInexistenteDevolve401() throws Exception {
		when(authService.autenticar(any(), any())).thenThrow(new CredenciaisInvalidasException("Email ou senha invalidos"));

		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{ "email": "inexistente@qualaboa.dev", "senha": "qualquer" }
						"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.detail").value("Email ou senha invalidos"));
	}

	@Test
	void loginComSenhaErradaDevolve401ComMesmaMensagemDoEmailInexistente() throws Exception {
		when(authService.autenticar(any(), any())).thenThrow(new CredenciaisInvalidasException("Email ou senha invalidos"));

		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{ "email": "local@qualaboa.dev", "senha": "senha-errada" }
						"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.detail").value("Email ou senha invalidos"));
	}

}
