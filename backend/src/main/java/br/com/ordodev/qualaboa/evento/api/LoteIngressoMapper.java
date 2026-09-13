package br.com.ordodev.qualaboa.evento.api;

import org.mapstruct.Mapper;

import br.com.ordodev.qualaboa.evento.LoteIngresso;

@Mapper(componentModel = "spring")
public interface LoteIngressoMapper {

	LoteIngressoResponse paraResponse(LoteIngresso lote);

}
