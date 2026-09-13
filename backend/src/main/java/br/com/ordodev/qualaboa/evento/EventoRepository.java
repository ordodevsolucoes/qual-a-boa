package br.com.ordodev.qualaboa.evento;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoRepository extends JpaRepository<Evento, UUID> {

	List<Evento> findBySituacao(SituacaoEvento situacao);

	List<Evento> findByLocalId(UUID localId);

}
