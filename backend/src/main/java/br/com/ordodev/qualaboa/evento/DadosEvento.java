package br.com.ordodev.qualaboa.evento;

import java.time.Instant;

// comando interno do servico: desacopla EventoService do DTO web, que so existe na camada de controller
public record DadosEvento(
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
		Integer capacidadeTotal) {
}
