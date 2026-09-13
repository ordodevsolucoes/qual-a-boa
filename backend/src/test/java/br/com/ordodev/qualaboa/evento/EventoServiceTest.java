package br.com.ordodev.qualaboa.evento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.throwable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.ordodev.qualaboa.excecao.AcessoNegadoException;
import br.com.ordodev.qualaboa.excecao.RegraDeNegocioException;
import br.com.ordodev.qualaboa.seguranca.UsuarioAutenticado;
import br.com.ordodev.qualaboa.usuario.PapelUsuario;
import br.com.ordodev.qualaboa.usuario.Usuario;
import br.com.ordodev.qualaboa.usuario.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

	private static final Instant AGORA = Instant.parse("2026-09-12T12:00:00Z");
	private static final UUID EVENTO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID LOCAL_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID OUTRO_LOCAL_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

	@Mock
	private EventoRepository eventoRepository;

	@Mock
	private LoteIngressoRepository loteIngressoRepository;

	@Mock
	private UsuarioRepository usuarioRepository;

	private Clock clock;
	private EventoService eventoService;
	private Usuario local;
	private UsuarioAutenticado usuarioAutenticado;

	@BeforeEach
	void configurar() {
		clock = Clock.fixed(AGORA, ZoneOffset.UTC);
		eventoService = new EventoService(eventoRepository, loteIngressoRepository, usuarioRepository, clock);
		local = comId(new Usuario("local@qualaboa.dev", "hash", "Local de Curso Semente", PapelUsuario.LOCAL_DE_CURSO, AGORA), LOCAL_ID);
		usuarioAutenticado = new UsuarioAutenticado(LOCAL_ID, PapelUsuario.LOCAL_DE_CURSO);
		lenient().when(eventoRepository.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
		lenient().when(usuarioRepository.getReferenceById(LOCAL_ID)).thenReturn(local);
	}

	private Usuario comId(Usuario usuario, UUID id) {
		ReflectionTestUtils.setField(usuario, "id", id);
		return usuario;
	}

	private Evento eventoValido(Instant inicio, Instant termino) {
		return new Evento(local, "Semana da Computacao", "Descricao do evento", "Computacao", inicio, termino,
				"Rua Um", "100", "Centro", "Recife", "PE", "50000000", 100);
	}

	private DadosEvento dadosValidos(Instant inicio, Instant termino) {
		return new DadosEvento("Semana da Computacao", "Descricao do evento", "Computacao", inicio, termino,
				"Rua Um", "100", "Centro", "Recife", "PE", "50000000", 100);
	}

	@Test
	void recusaEventoComDataDeInicioNoPassado() {
		DadosEvento dados = dadosValidos(AGORA.minusSeconds(60), AGORA.plusSeconds(3600));

		assertThatThrownBy(() -> eventoService.criar(dados, usuarioAutenticado))
				.asInstanceOf(throwable(RegraDeNegocioException.class))
				.extracting(RegraDeNegocioException::getRegra)
				.isEqualTo("RN03");
	}

	@Test
	void recusaEventoComTerminoAnteriorOuIgualAoInicio() {
		Instant inicio = AGORA.plusSeconds(3600);
		DadosEvento dados = dadosValidos(inicio, inicio);

		assertThatThrownBy(() -> eventoService.criar(dados, usuarioAutenticado))
				.asInstanceOf(throwable(RegraDeNegocioException.class))
				.extracting(RegraDeNegocioException::getRegra)
				.isEqualTo("RN03");
	}

	@ParameterizedTest
	@ValueSource(strings = { "titulo", "descricao", "areaConhecimento", "inicio", "termino", "logradouro", "numero",
			"bairro", "cidade", "uf", "cep", "capacidadeTotal" })
	void recusaEventoComCampoObrigatorioAusente(String campoAusente) {
		DadosEvento dados = dadosComCampoAusente(campoAusente);

		assertThatThrownBy(() -> eventoService.criar(dados, usuarioAutenticado))
				.asInstanceOf(throwable(RegraDeNegocioException.class))
				.extracting(RegraDeNegocioException::getRegra)
				.isEqualTo("RN02");
	}

	private DadosEvento dadosComCampoAusente(String campoAusente) {
		Instant inicio = AGORA.plusSeconds(3600);
		Instant termino = AGORA.plusSeconds(7200);
		return new DadosEvento(
				campo(campoAusente, "titulo", "Semana da Computacao"),
				campo(campoAusente, "descricao", "Descricao do evento"),
				campo(campoAusente, "areaConhecimento", "Computacao"),
				campoAusente.equals("inicio") ? null : inicio,
				campoAusente.equals("termino") ? null : termino,
				campo(campoAusente, "logradouro", "Rua Um"),
				campo(campoAusente, "numero", "100"),
				campo(campoAusente, "bairro", "Centro"),
				campo(campoAusente, "cidade", "Recife"),
				campo(campoAusente, "uf", "PE"),
				campo(campoAusente, "cep", "50000000"),
				campoAusente.equals("capacidadeTotal") ? null : 100);
	}

	private String campo(String campoAusente, String nomeDoCampo, String valorValido) {
		return campoAusente.equals(nomeDoCampo) ? null : valorValido;
	}

	@Test
	void recusaPublicacaoDeEventoSemLote() {
		Evento evento = eventoValido(AGORA.plusSeconds(3600), AGORA.plusSeconds(7200));
		when(eventoRepository.findById(EVENTO_ID)).thenReturn(Optional.of(evento));
		when(loteIngressoRepository.existsByEventoId(EVENTO_ID)).thenReturn(false);

		assertThatThrownBy(() -> eventoService.publicar(EVENTO_ID, usuarioAutenticado))
				.asInstanceOf(throwable(RegraDeNegocioException.class))
				.extracting(RegraDeNegocioException::getRegra)
				.isEqualTo("RN04");
	}

	@Test
	void permitePublicacaoDeEventoComLote() {
		Evento evento = eventoValido(AGORA.plusSeconds(3600), AGORA.plusSeconds(7200));
		when(eventoRepository.findById(EVENTO_ID)).thenReturn(Optional.of(evento));
		when(loteIngressoRepository.existsByEventoId(EVENTO_ID)).thenReturn(true);

		Evento publicado = eventoService.publicar(EVENTO_ID, usuarioAutenticado);

		assertThat(publicado.getSituacao()).isEqualTo(SituacaoEvento.PUBLICADO);
	}

	@Test
	void listaApenasEventosPublicadosNaConsultaPublica() {
		Evento publicado = eventoValido(AGORA.plusSeconds(3600), AGORA.plusSeconds(7200));
		when(eventoRepository.findBySituacao(SituacaoEvento.PUBLICADO)).thenReturn(List.of(publicado));

		List<Evento> resultado = eventoService.listarPublicados();

		assertThat(resultado).containsExactly(publicado);
	}

	@Test
	void listaApenasEventosDoProprioTitular() {
		Evento evento = eventoValido(AGORA.plusSeconds(3600), AGORA.plusSeconds(7200));
		when(eventoRepository.findByLocalId(LOCAL_ID)).thenReturn(List.of(evento));

		List<Evento> resultado = eventoService.listarDoUsuario(usuarioAutenticado);

		assertThat(resultado).containsExactly(evento);
	}

	@Test
	void permiteAcessoAEventoProprio() {
		Evento evento = eventoValido(AGORA.plusSeconds(3600), AGORA.plusSeconds(7200));
		when(eventoRepository.findById(EVENTO_ID)).thenReturn(Optional.of(evento));

		Evento encontrado = eventoService.buscarPorId(EVENTO_ID, usuarioAutenticado);

		assertThat(encontrado).isSameAs(evento);
	}

	@Test
	void recusaAcessoAEventoDeOutroTitularEAEventoInexistenteComAMesmaMensagem() {
		Usuario outroLocal = comId(new Usuario("outro@qualaboa.dev", "hash", "Outro Local", PapelUsuario.LOCAL_DE_CURSO, AGORA), OUTRO_LOCAL_ID);
		Evento eventoDeOutro = new Evento(outroLocal, "Semana de Outro", "Descricao", "Area", AGORA.plusSeconds(3600),
				AGORA.plusSeconds(7200), "Rua Dois", "200", "Bairro", "Cidade", "PE", "50000000", 50);
		when(eventoRepository.findById(EVENTO_ID)).thenReturn(Optional.of(eventoDeOutro));

		String mensagemComEventoDeOutro = capturarMensagem(() -> eventoService.buscarPorId(EVENTO_ID, usuarioAutenticado));

		when(eventoRepository.findById(EVENTO_ID)).thenReturn(Optional.empty());

		String mensagemComEventoInexistente = capturarMensagem(() -> eventoService.buscarPorId(EVENTO_ID, usuarioAutenticado));

		assertThat(mensagemComEventoDeOutro).isEqualTo(mensagemComEventoInexistente);
	}

	private String capturarMensagem(Runnable execucao) {
		return org.assertj.core.api.Assertions.catchThrowableOfType(execucao::run, AcessoNegadoException.class).getMessage();
	}

	@Test
	void atualizaDadosDeEventoProprio() {
		Evento evento = eventoValido(AGORA.plusSeconds(3600), AGORA.plusSeconds(7200));
		when(eventoRepository.findById(EVENTO_ID)).thenReturn(Optional.of(evento));
		DadosEvento dados = dadosValidos(AGORA.plusSeconds(10800), AGORA.plusSeconds(14400));

		Evento atualizado = eventoService.atualizar(EVENTO_ID, dados, usuarioAutenticado);

		assertThat(atualizado.getInicio()).isEqualTo(AGORA.plusSeconds(10800));
	}

	@Test
	void recusaCriacaoQuandoPapelNaoELocalDeCurso() {
		UsuarioAutenticado participante = new UsuarioAutenticado(LOCAL_ID, PapelUsuario.PARTICIPANTE);
		DadosEvento dados = dadosValidos(AGORA.plusSeconds(3600), AGORA.plusSeconds(7200));

		assertThatThrownBy(() -> eventoService.criar(dados, participante))
				.isInstanceOf(AcessoNegadoException.class);
	}

	@Test
	void recusaPublicacaoQuandoPapelNaoELocalDeCurso() {
		UsuarioAutenticado participante = new UsuarioAutenticado(LOCAL_ID, PapelUsuario.PARTICIPANTE);

		assertThatThrownBy(() -> eventoService.publicar(EVENTO_ID, participante))
				.isInstanceOf(AcessoNegadoException.class);
	}

	@Test
	void recusaAtualizacaoDeEventoDeOutroTitular() {
		Usuario outroLocal = comId(new Usuario("outro@qualaboa.dev", "hash", "Outro Local", PapelUsuario.LOCAL_DE_CURSO, AGORA), OUTRO_LOCAL_ID);
		Evento eventoDeOutro = new Evento(outroLocal, "Semana de Outro", "Descricao", "Area", AGORA.plusSeconds(3600),
				AGORA.plusSeconds(7200), "Rua Dois", "200", "Bairro", "Cidade", "PE", "50000000", 50);
		when(eventoRepository.findById(EVENTO_ID)).thenReturn(Optional.of(eventoDeOutro));
		DadosEvento dados = dadosValidos(AGORA.plusSeconds(10800), AGORA.plusSeconds(14400));

		assertThatThrownBy(() -> eventoService.atualizar(EVENTO_ID, dados, usuarioAutenticado))
				.isInstanceOf(AcessoNegadoException.class);
	}

}
