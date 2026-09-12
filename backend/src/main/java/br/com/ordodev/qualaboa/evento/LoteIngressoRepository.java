package br.com.ordodev.qualaboa.evento;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoteIngressoRepository extends JpaRepository<LoteIngresso, UUID> {

	boolean existsByEventoId(UUID eventoId);

}
