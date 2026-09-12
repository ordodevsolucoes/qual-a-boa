package br.com.ordodev.qualaboa.evento;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;

import org.springframework.stereotype.Service;

import br.com.ordodev.qualaboa.excecao.RegraDeNegocioException;

@Service
public class LoteIngressoService {

	private final LoteIngressoRepository loteIngressoRepository;
	private final Clock clock;

	public LoteIngressoService(LoteIngressoRepository loteIngressoRepository, Clock clock) {
		this.loteIngressoRepository = loteIngressoRepository;
		this.clock = clock;
	}

	public LoteIngresso criar(Evento evento, LoteIngresso lote) {
		validarCamposObrigatorios(lote);
		validarVigencia(lote, evento);
		List<LoteIngresso> lotesExistentes = loteIngressoRepository.findByEventoId(evento.getId());
		validarCapacidade(evento, lotesExistentes, lote);
		lote.registrarCriacao(clock.instant());
		return loteIngressoRepository.save(lote);
	}

	private void validarCapacidade(Evento evento, List<LoteIngresso> lotesExistentes, LoteIngresso lote) {
		int quantidadeExistente = lotesExistentes.stream().mapToInt(LoteIngresso::getQuantidade).sum();
		int somaTotal = quantidadeExistente + lote.getQuantidade();
		if (somaTotal > evento.getCapacidadeTotal()) {
			int vagasRestantes = Math.max(evento.getCapacidadeTotal() - quantidadeExistente, 0);
			throw new RegraDeNegocioException("RN07", "quantidade",
					"Lote excede a capacidade do evento, vagas restantes: " + vagasRestantes);
		}
	}

	private void validarVigencia(LoteIngresso lote, Evento evento) {
		if (!lote.getVigenciaFim().isAfter(lote.getVigenciaInicio())) {
			throw new RegraDeNegocioException("RN06", "vigenciaFim", "Fim da vigencia deve ser posterior ao inicio dela");
		}
		if (lote.getVigenciaFim().isAfter(evento.getInicio())) {
			throw new RegraDeNegocioException("RN06", "vigenciaFim", "Fim da vigencia deve ser anterior ou igual ao inicio do evento");
		}
	}

	private void validarCamposObrigatorios(LoteIngresso lote) {
		if (lote.getNome() == null || lote.getNome().isBlank()) {
			throw new RegraDeNegocioException("RN05", "nome", "Campo obrigatorio nao informado: nome");
		}
		if (lote.getQuantidade() == null || lote.getQuantidade() <= 0) {
			throw new RegraDeNegocioException("RN05", "quantidade", "Quantidade deve ser maior que zero");
		}
		if (lote.getPreco() == null || lote.getPreco().compareTo(BigDecimal.ZERO) < 0) {
			throw new RegraDeNegocioException("RN05", "preco", "Preco nao pode ser negativo");
		}
		if (lote.getVigenciaInicio() == null) {
			throw new RegraDeNegocioException("RN05", "vigenciaInicio", "Campo obrigatorio nao informado: vigenciaInicio");
		}
		if (lote.getVigenciaFim() == null) {
			throw new RegraDeNegocioException("RN05", "vigenciaFim", "Campo obrigatorio nao informado: vigenciaFim");
		}
	}

}
