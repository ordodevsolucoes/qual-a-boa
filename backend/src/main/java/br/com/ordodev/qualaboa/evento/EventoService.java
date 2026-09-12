package br.com.ordodev.qualaboa.evento;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Service;

import br.com.ordodev.qualaboa.excecao.RegraDeNegocioException;

@Service
public class EventoService {

	private final EventoRepository eventoRepository;
	private final Clock clock;

	public EventoService(EventoRepository eventoRepository, Clock clock) {
		this.eventoRepository = eventoRepository;
		this.clock = clock;
	}

	public Evento criar(Evento evento) {
		Instant agora = clock.instant();
		validarCoerenciaDeDatas(evento, agora);
		evento.registrarCriacao(agora);
		return eventoRepository.save(evento);
	}

	private void validarCoerenciaDeDatas(Evento evento, Instant agora) {
		if (evento.getInicio().isBefore(agora)) {
			throw new RegraDeNegocioException("RN03", "inicio", "Data de inicio nao pode ser anterior ao instante da criacao");
		}
		if (!evento.getTermino().isAfter(evento.getInicio())) {
			throw new RegraDeNegocioException("RN03", "termino", "Termino deve ser posterior ao inicio");
		}
	}

}
