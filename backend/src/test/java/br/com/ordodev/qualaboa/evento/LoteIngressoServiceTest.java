package br.com.ordodev.qualaboa.evento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.throwable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

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
class LoteIngressoServiceTest {

	private static final Instant AGORA = Instant.parse("2026-09-12T12:00:00Z");

	@Mock
	private LoteIngressoRepository loteIngressoRepository;

	private Clock clock;
	private LoteIngressoService loteIngressoService;
	private Evento evento;

	@BeforeEach
	void configurar() {
		clock = Clock.fixed(AGORA, ZoneOffset.UTC);
		loteIngressoService = new LoteIngressoService(loteIngressoRepository, clock);
		Usuario local = new Usuario("local@qualaboa.dev", "hash", "Local Semente", PapelUsuario.LOCAL_DE_CURSO, AGORA);
		evento = new Evento(local, "Semana da Computacao", "Descricao", "Computacao",
				AGORA.plusSeconds(7 * 24 * 3600), AGORA.plusSeconds(10 * 24 * 3600),
				"Rua Um", "100", "Centro", "Recife", "PE", "50000000", 100);
		lenient().when(loteIngressoRepository.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
		lenient().when(loteIngressoRepository.findByEventoId(any())).thenReturn(List.of());
	}

	@ParameterizedTest
	@ValueSource(strings = { "quantidadeZero", "precoNegativo" })
	void recusaLoteComQuantidadeZeroOuPrecoNegativo(String cenario) {
		Instant vigenciaInicio = AGORA.plusSeconds(3600);
		Instant vigenciaFim = evento.getInicio();
		Integer quantidade = cenario.equals("quantidadeZero") ? 0 : 10;
		BigDecimal preco = cenario.equals("precoNegativo") ? BigDecimal.valueOf(-1) : BigDecimal.TEN;
		LoteIngresso lote = new LoteIngresso(evento, "Lote", quantidade, preco, vigenciaInicio, vigenciaFim);

		assertThatThrownBy(() -> loteIngressoService.criar(evento, lote))
				.asInstanceOf(throwable(RegraDeNegocioException.class))
				.extracting(RegraDeNegocioException::getRegra)
				.isEqualTo("RN05");
	}

	@ParameterizedTest
	@ValueSource(strings = { "nome", "vigenciaInicio", "vigenciaFim" })
	void recusaLoteComCampoObrigatorioAusente(String campoAusente) {
		Instant vigenciaInicio = campoAusente.equals("vigenciaInicio") ? null : AGORA.plusSeconds(3600);
		Instant vigenciaFim = campoAusente.equals("vigenciaFim") ? null : evento.getInicio();
		String nome = campoAusente.equals("nome") ? null : "Lote";
		LoteIngresso lote = new LoteIngresso(evento, nome, 10, BigDecimal.TEN, vigenciaInicio, vigenciaFim);

		assertThatThrownBy(() -> loteIngressoService.criar(evento, lote))
				.asInstanceOf(throwable(RegraDeNegocioException.class))
				.extracting(RegraDeNegocioException::getRegra)
				.isEqualTo("RN05");
	}

	@Test
	void aceitaLoteComVigenciaTerminandoNoInicioDoEvento() {
		LoteIngresso lote = new LoteIngresso(evento, "Lote", 10, BigDecimal.TEN, AGORA.plusSeconds(3600), evento.getInicio());

		LoteIngresso criado = loteIngressoService.criar(evento, lote);

		assertThat(criado.getVigenciaFim()).isEqualTo(evento.getInicio());
	}

	@Test
	void recusaLoteComVigenciaAlemDoInicioDoEvento() {
		Instant vigenciaFim = evento.getInicio().plusSeconds(1);
		LoteIngresso lote = new LoteIngresso(evento, "Lote", 10, BigDecimal.TEN, AGORA.plusSeconds(3600), vigenciaFim);

		assertThatThrownBy(() -> loteIngressoService.criar(evento, lote))
				.asInstanceOf(throwable(RegraDeNegocioException.class))
				.extracting(RegraDeNegocioException::getRegra)
				.isEqualTo("RN06");
	}

	@Test
	void recusaLoteComVigenciaFimAnteriorOuIgualAoInicio() {
		Instant vigenciaInicio = AGORA.plusSeconds(3600);
		LoteIngresso lote = new LoteIngresso(evento, "Lote", 10, BigDecimal.TEN, vigenciaInicio, vigenciaInicio);

		assertThatThrownBy(() -> loteIngressoService.criar(evento, lote))
				.asInstanceOf(throwable(RegraDeNegocioException.class))
				.extracting(RegraDeNegocioException::getRegra)
				.isEqualTo("RN06");
	}

}
