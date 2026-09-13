package br.com.ordodev.qualaboa.evento.api;

import java.time.Instant;
import java.util.UUID;

public record EventoResponse(
		UUID id,
		String titulo,
		String descricao,
		String areaConhecimento,
		Instant inicio,
		Instant termino,
		String logradouro,
		String numero,
		String bairro,
		String cidade,
		String uf,
		String cep,
		Integer capacidadeTotal,
		String situacao,
		Instant criadoEm,
		Instant atualizadoEm) {
}
