package br.com.ordodev.qualaboa.evento.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LoteIngressoResponse(
		UUID id,
		String nome,
		Integer quantidade,
		BigDecimal preco,
		Instant vigenciaInicio,
		Instant vigenciaFim) {
}
