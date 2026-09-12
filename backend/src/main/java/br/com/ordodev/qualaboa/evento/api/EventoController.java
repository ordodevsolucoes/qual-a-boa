package br.com.ordodev.qualaboa.evento.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.ordodev.qualaboa.evento.Evento;
import br.com.ordodev.qualaboa.evento.EventoService;
import br.com.ordodev.qualaboa.evento.LoteIngresso;
import br.com.ordodev.qualaboa.evento.LoteIngressoService;
import br.com.ordodev.qualaboa.seguranca.UsuarioAutenticado;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/eventos")
public class EventoController {

	private final EventoService eventoService;
	private final LoteIngressoService loteIngressoService;
	private final EventoMapper eventoMapper;
	private final LoteIngressoMapper loteIngressoMapper;

	public EventoController(EventoService eventoService, LoteIngressoService loteIngressoService,
			EventoMapper eventoMapper, LoteIngressoMapper loteIngressoMapper) {
		this.eventoService = eventoService;
		this.loteIngressoService = loteIngressoService;
		this.eventoMapper = eventoMapper;
		this.loteIngressoMapper = loteIngressoMapper;
	}

	@PostMapping
	public ResponseEntity<EventoResponse> criar(@Valid @RequestBody EventoRequest request,
			@AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado) {
		Evento evento = eventoService.criar(eventoMapper.paraDados(request), usuarioAutenticado);
		return ResponseEntity.created(URI.create("/api/v1/eventos/" + evento.getId()))
				.body(eventoMapper.paraResponse(evento));
	}

	@GetMapping
	public List<EventoResponse> listar(@AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado) {
		return eventoService.listarDoUsuario(usuarioAutenticado).stream().map(eventoMapper::paraResponse).toList();
	}

	@GetMapping("/{id}")
	public EventoResponse buscarPorId(@PathVariable UUID id, @AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado) {
		return eventoMapper.paraResponse(eventoService.buscarPorId(id, usuarioAutenticado));
	}

	@PutMapping("/{id}")
	public EventoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody EventoRequest request,
			@AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado) {
		Evento evento = eventoService.atualizar(id, eventoMapper.paraDados(request), usuarioAutenticado);
		return eventoMapper.paraResponse(evento);
	}

	@PostMapping("/{id}/lotes")
	public ResponseEntity<LoteIngressoResponse> criarLote(@PathVariable UUID id, @Valid @RequestBody LoteIngressoRequest request,
			@AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado) {
		Evento evento = eventoService.buscarPorId(id, usuarioAutenticado);
		LoteIngresso lote = new LoteIngresso(evento, request.nome(), request.quantidade(), request.preco(),
				request.vigenciaInicio(), request.vigenciaFim());
		LoteIngresso criado = loteIngressoService.criar(evento, lote);
		return ResponseEntity.created(URI.create("/api/v1/eventos/" + id + "/lotes/" + criado.getId()))
				.body(loteIngressoMapper.paraResponse(criado));
	}

	@PostMapping("/{id}/publicacao")
	public EventoResponse publicar(@PathVariable UUID id, @AuthenticationPrincipal UsuarioAutenticado usuarioAutenticado) {
		return eventoMapper.paraResponse(eventoService.publicar(id, usuarioAutenticado));
	}

}
