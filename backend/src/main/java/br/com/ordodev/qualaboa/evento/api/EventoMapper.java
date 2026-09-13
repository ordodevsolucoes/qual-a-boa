package br.com.ordodev.qualaboa.evento.api;

import org.mapstruct.Mapper;

import br.com.ordodev.qualaboa.evento.DadosEvento;
import br.com.ordodev.qualaboa.evento.Evento;

@Mapper(componentModel = "spring")
public interface EventoMapper {

	DadosEvento paraDados(EventoRequest request);

	EventoResponse paraResponse(Evento evento);

}
