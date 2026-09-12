package br.com.ordodev.qualaboa.evento;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.throwable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.ordodev.qualaboa.excecao.RegraDeNegocioException;
import br.com.ordodev.qualaboa.usuario.PapelUsuario;
import br.com.ordodev.qualaboa.usuario.Usuario;

@ExtendWith(MockitoExtension.class)
class EventoServiceTest {

	private static final Instant AGORA = Instant.parse("2026-09-12T12:00:00Z");

	@Mock
	private EventoRepository eventoRepository;

	private Clock clock;
	private EventoService eventoService;
	private Usuario local;

	@BeforeEach
	void configurar() {
		clock = Clock.fixed(AGORA, ZoneOffset.UTC);
		eventoService = new EventoService(eventoRepository, clock);
		local = new Usuario("local@qualaboa.dev", "hash", "Local de Curso Semente", PapelUsuario.LOCAL_DE_CURSO, AGORA);
		lenient().when(eventoRepository.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
	}

	private Evento eventoValido(Instant inicio, Instant termino) {
		return new Evento(local, "Semana da Computacao", "Descricao do evento", "Computacao", inicio, termino,
				"Rua Um", "100", "Centro", "Recife", "PE", "50000000", 100);
	}

	@Test
	void recusaEventoComDataDeInicioNoPassado() {
		Evento evento = eventoValido(AGORA.minusSeconds(60), AGORA.plusSeconds(3600));

		assertThatThrownBy(() -> eventoService.criar(evento))
				.asInstanceOf(throwable(RegraDeNegocioException.class))
				.extracting(RegraDeNegocioException::getRegra)
				.isEqualTo("RN03");
	}

	@Test
	void recusaEventoComTerminoAnteriorOuIgualAoInicio() {
		Instant inicio = AGORA.plusSeconds(3600);
		Evento evento = eventoValido(inicio, inicio);

		assertThatThrownBy(() -> eventoService.criar(evento))
				.asInstanceOf(throwable(RegraDeNegocioException.class))
				.extracting(RegraDeNegocioException::getRegra)
				.isEqualTo("RN03");
	}

	@ParameterizedTest
	@ValueSource(strings = { "titulo", "descricao", "areaConhecimento", "inicio", "termino", "logradouro", "numero",
			"bairro", "cidade", "uf", "cep", "capacidadeTotal" })
	void recusaEventoComCampoObrigatorioAusente(String campoAusente) {
		Evento evento = eventoComCampoAusente(campoAusente);

		assertThatThrownBy(() -> eventoService.criar(evento))
				.asInstanceOf(throwable(RegraDeNegocioException.class))
				.extracting(RegraDeNegocioException::getRegra)
				.isEqualTo("RN02");
	}

	private Evento eventoComCampoAusente(String campoAusente) {
		Instant inicio = AGORA.plusSeconds(3600);
		Instant termino = AGORA.plusSeconds(7200);
		return new Evento(
				local,
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

}
