package br.com.ordodev.qualaboa.evento;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoteIngressoRepository extends JpaRepository<LoteIngresso, UUID> {

	boolean existsByEventoId(UUID eventoId);

	List<LoteIngresso> findByEventoId(UUID eventoId);

}
