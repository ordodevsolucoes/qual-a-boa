package br.com.ordodev.qualaboa.evento.api;

import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import br.com.ordodev.qualaboa.evento.Evento;
import br.com.ordodev.qualaboa.evento.EventoService;
import br.com.ordodev.qualaboa.evento.LoteIngresso;
import br.com.ordodev.qualaboa.evento.LoteIngressoService;
import br.com.ordodev.qualaboa.excecao.RegraDeNegocioException;
import br.com.ordodev.qualaboa.seguranca.UsuarioAutenticado;
import br.com.ordodev.qualaboa.usuario.PapelUsuario;
import br.com.ordodev.qualaboa.usuario.Usuario;

@WebMvcTest(EventoController.class)
@Import({ EventoMapperImpl.class, LoteIngressoMapperImpl.class })
class EventoControllerTest {

	private static final Instant AGORA = Instant.parse("2026-09-12T12:00:00Z");
	private static final UUID LOCAL_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private EventoService eventoService;

	@MockitoBean
	private LoteIngressoService loteIngressoService;

	private org.springframework.test.web.servlet.request.RequestPostProcessor comoLocalDeCurso() {
		UsuarioAutenticado usuarioAutenticado = new UsuarioAutenticado(LOCAL_ID, PapelUsuario.LOCAL_DE_CURSO);
		return authentication(new UsernamePasswordAuthenticationToken(usuarioAutenticado, null,
				List.of(new SimpleGrantedAuthority("ROLE_LOCAL_DE_CURSO"))));
	}

	private Evento eventoFixture() {
		Usuario local = new Usuario("local@qualaboa.dev", "hash", "Local Semente", PapelUsuario.LOCAL_DE_CURSO, AGORA);
		ReflectionTestUtils.setField(local, "id", LOCAL_ID);
		Evento evento = new Evento(local, "Semana da Computacao", "Descricao do evento", "Computacao",
				AGORA.plusSeconds(3600), AGORA.plusSeconds(7200), "Rua Um", "100", "Centro", "Recife", "PE",
				"50000000", 100);
		ReflectionTestUtils.setField(evento, "id", UUID.fromString("11111111-1111-1111-1111-111111111111"));
		ReflectionTestUtils.setField(evento, "criadoEm", AGORA);
		ReflectionTestUtils.setField(evento, "atualizadoEm", AGORA);
		return evento;
	}

	private String corpoValido() {
		return """
				{
					"titulo": "Semana da Computacao",
					"descricao": "Descricao do evento",
					"areaConhecimento": "Computacao",
					"inicio": "2026-09-13T12:00:00Z",
					"termino": "2026-09-13T18:00:00Z",
					"logradouro": "Rua Um",
					"numero": "100",
					"bairro": "Centro",
					"cidade": "Recife",
					"uf": "PE",
					"cep": "50000000",
					"capacidadeTotal": 100
				}
				""";
	}

	@Test
	void criarDevolve201ComLocation() throws Exception {
		when(eventoService.criar(any(), any())).thenReturn(eventoFixture());

		mockMvc.perform(post("/api/v1/eventos")
				.with(comoLocalDeCurso()).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpoValido()))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", "/api/v1/eventos/11111111-1111-1111-1111-111111111111"))
				.andExpect(jsonPath("$.situacao").value("RASCUNHO"));
	}

	@Test
	void devolve400ComDetalhePorCampo() throws Exception {
		String corpoSemTitulo = """
				{
					"titulo": "",
					"descricao": "Descricao do evento",
					"areaConhecimento": "Computacao",
					"inicio": "2026-09-13T12:00:00Z",
					"termino": "2026-09-13T18:00:00Z",
					"logradouro": "Rua Um",
					"numero": "100",
					"bairro": "Centro",
					"cidade": "Recife",
					"uf": "PE",
					"cep": "50000000",
					"capacidadeTotal": 100
				}
				""";

		mockMvc.perform(post("/api/v1/eventos")
				.with(comoLocalDeCurso()).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpoSemTitulo))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.errors[0].campo").value("titulo"))
				.andExpect(content().string(not(org.hamcrest.Matchers.containsString("Exception"))))
				.andExpect(content().string(not(org.hamcrest.Matchers.containsString("org.springframework"))))
				.andExpect(content().string(not(org.hamcrest.Matchers.containsString("SQL"))));
	}

	@Test
	void listarDevolveApenasEventosDoUsuarioAutenticado() throws Exception {
		when(eventoService.listarDoUsuario(any())).thenReturn(List.of(eventoFixture()));

		mockMvc.perform(get("/api/v1/eventos").with(comoLocalDeCurso()).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));
	}

	@Test
	void publicarSemLoteDevolve422ComRegra() throws Exception {
		when(eventoService.publicar(any(), any()))
				.thenThrow(new RegraDeNegocioException("RN04", "Evento sem lote nao pode ser publicado"));

		mockMvc.perform(post("/api/v1/eventos/11111111-1111-1111-1111-111111111111/publicacao").with(comoLocalDeCurso()).with(csrf()))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.regra").value("RN04"));
	}

	@Test
	void buscarPorIdDevolve200() throws Exception {
		when(eventoService.buscarPorId(any(), any())).thenReturn(eventoFixture());

		mockMvc.perform(get("/api/v1/eventos/11111111-1111-1111-1111-111111111111").with(comoLocalDeCurso()).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.titulo").value("Semana da Computacao"));
	}

	@Test
	void atualizarDevolve200() throws Exception {
		when(eventoService.atualizar(any(), any(), any())).thenReturn(eventoFixture());

		mockMvc.perform(put("/api/v1/eventos/11111111-1111-1111-1111-111111111111")
				.with(comoLocalDeCurso()).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpoValido()))
				.andExpect(status().isOk());
	}

	@Test
	void criarLoteDevolve201() throws Exception {
		when(eventoService.buscarPorId(any(), any())).thenReturn(eventoFixture());
		LoteIngresso lote = new LoteIngresso(eventoFixture(), "Lote unico", 100, BigDecimal.TEN, AGORA, AGORA.plusSeconds(1800));
		ReflectionTestUtils.setField(lote, "id", UUID.fromString("44444444-4444-4444-4444-444444444444"));
		when(loteIngressoService.criar(any(), any())).thenReturn(lote);

		String corpoLote = """
				{
					"nome": "Lote unico",
					"quantidade": 100,
					"preco": 10.00,
					"vigenciaInicio": "2026-09-12T12:00:00Z",
					"vigenciaFim": "2026-09-12T12:30:00Z"
				}
				""";

		mockMvc.perform(post("/api/v1/eventos/11111111-1111-1111-1111-111111111111/lotes")
				.with(comoLocalDeCurso()).with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content(corpoLote))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.nome").value("Lote unico"));
	}

}
