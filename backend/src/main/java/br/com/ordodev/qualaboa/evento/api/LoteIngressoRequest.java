package br.com.ordodev.qualaboa.evento.api;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record LoteIngressoRequest(
		@NotBlank String nome,
		@NotNull @Positive Integer quantidade,
		@NotNull @PositiveOrZero BigDecimal preco,
		@NotNull Instant vigenciaInicio,
		@NotNull Instant vigenciaFim) {
}
