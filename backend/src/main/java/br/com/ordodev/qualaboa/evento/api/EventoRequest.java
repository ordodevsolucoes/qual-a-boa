package br.com.ordodev.qualaboa.evento.api;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EventoRequest(
		@NotBlank @Size(max = 150) String titulo,
		@NotBlank @Size(max = 2000) String descricao,
		@NotBlank String areaConhecimento,
		@NotNull Instant inicio,
		@NotNull Instant termino,
		@NotBlank String logradouro,
		@NotBlank String numero,
		@NotBlank String bairro,
		@NotBlank String cidade,
		@NotBlank @Pattern(regexp = "[A-Za-z]{2}", message = "deve ter exatamente 2 letras") String uf,
		@NotBlank @Pattern(regexp = "\\d{8}", message = "deve ter exatamente 8 digitos") String cep,
		@NotNull @Positive Integer capacidadeTotal) {
}
