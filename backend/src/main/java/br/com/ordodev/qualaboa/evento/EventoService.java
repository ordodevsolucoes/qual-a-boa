package br.com.ordodev.qualaboa.evento;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.ordodev.qualaboa.excecao.AcessoNegadoException;
import br.com.ordodev.qualaboa.excecao.RegraDeNegocioException;
import br.com.ordodev.qualaboa.seguranca.UsuarioAutenticado;
import br.com.ordodev.qualaboa.usuario.Usuario;
import br.com.ordodev.qualaboa.usuario.UsuarioRepository;

@Service
public class EventoService {

	private final EventoRepository eventoRepository;
	private final LoteIngressoRepository loteIngressoRepository;
	private final UsuarioRepository usuarioRepository;
	private final Clock clock;

	public EventoService(EventoRepository eventoRepository, LoteIngressoRepository loteIngressoRepository,
			UsuarioRepository usuarioRepository, Clock clock) {
		this.eventoRepository = eventoRepository;
		this.loteIngressoRepository = loteIngressoRepository;
		this.usuarioRepository = usuarioRepository;
		this.clock = clock;
	}

	public Evento criar(DadosEvento dados, UsuarioAutenticado usuarioAutenticado) {
		validarCamposObrigatorios(dados);
		Instant agora = clock.instant();
		validarCoerenciaDeDatas(dados.inicio(), dados.termino(), agora);
		Usuario local = usuarioRepository.getReferenceById(usuarioAutenticado.id());
		Evento evento = new Evento(local, dados.titulo(), dados.descricao(), dados.areaConhecimento(),
				dados.inicio(), dados.termino(), dados.logradouro(), dados.numero(), dados.bairro(), dados.cidade(),
				dados.uf(), dados.cep(), dados.capacidadeTotal());
		evento.registrarCriacao(agora);
		return eventoRepository.save(evento);
	}

	public Evento buscarPorId(UUID eventoId, UsuarioAutenticado usuarioAutenticado) {
		return buscarEventoDoTitular(eventoId, usuarioAutenticado);
	}

	public List<Evento> listarDoUsuario(UsuarioAutenticado usuarioAutenticado) {
		return eventoRepository.findByLocalId(usuarioAutenticado.id());
	}

	public Evento atualizar(UUID eventoId, DadosEvento dados, UsuarioAutenticado usuarioAutenticado) {
		Evento evento = buscarEventoDoTitular(eventoId, usuarioAutenticado);
		validarCamposObrigatorios(dados);
		Instant agora = clock.instant();
		validarCoerenciaDeDatas(dados.inicio(), dados.termino(), agora);
		evento.atualizarDados(dados.titulo(), dados.descricao(), dados.areaConhecimento(), dados.inicio(),
				dados.termino(), dados.logradouro(), dados.numero(), dados.bairro(), dados.cidade(), dados.uf(),
				dados.cep(), dados.capacidadeTotal(), agora);
		return eventoRepository.save(evento);
	}

	public Evento publicar(UUID eventoId, UsuarioAutenticado usuarioAutenticado) {
		Evento evento = buscarEventoDoTitular(eventoId, usuarioAutenticado);
		if (!loteIngressoRepository.existsByEventoId(eventoId)) {
			throw new RegraDeNegocioException("RN04", "Evento sem lote nao pode ser publicado");
		}
		evento.publicar(clock.instant());
		return eventoRepository.save(evento);
	}

	public List<Evento> listarPublicados() {
		return eventoRepository.findBySituacao(SituacaoEvento.PUBLICADO);
	}

	// RN09: mesma excecao para "nao existe" e para "existe mas nao e seu", de proposito,
	// para as duas respostas serem indistinguiveis
	private Evento buscarEventoDoTitular(UUID eventoId, UsuarioAutenticado usuarioAutenticado) {
		return eventoRepository.findById(eventoId)
				.filter(evento -> evento.getLocal().getId().equals(usuarioAutenticado.id()))
				.orElseThrow(() -> new AcessoNegadoException("Evento nao encontrado"));
	}

	private void validarCamposObrigatorios(DadosEvento dados) {
		exigirPreenchido(dados.titulo(), "titulo");
		exigirPreenchido(dados.descricao(), "descricao");
		exigirPreenchido(dados.areaConhecimento(), "areaConhecimento");
		exigirPresente(dados.inicio(), "inicio");
		exigirPresente(dados.termino(), "termino");
		exigirPreenchido(dados.logradouro(), "logradouro");
		exigirPreenchido(dados.numero(), "numero");
		exigirPreenchido(dados.bairro(), "bairro");
		exigirPreenchido(dados.cidade(), "cidade");
		exigirPreenchido(dados.uf(), "uf");
		exigirPreenchido(dados.cep(), "cep");
		exigirPresente(dados.capacidadeTotal(), "capacidadeTotal");
	}

	private void exigirPreenchido(String valor, String campo) {
		if (valor == null || valor.isBlank()) {
			throw new RegraDeNegocioException("RN02", campo, "Campo obrigatorio nao informado: " + campo);
		}
	}

	private void exigirPresente(Object valor, String campo) {
		if (valor == null) {
			throw new RegraDeNegocioException("RN02", campo, "Campo obrigatorio nao informado: " + campo);
		}
	}

	private void validarCoerenciaDeDatas(Instant inicio, Instant termino, Instant agora) {
		if (inicio.isBefore(agora)) {
			throw new RegraDeNegocioException("RN03", "inicio", "Data de inicio nao pode ser anterior ao instante da criacao");
		}
		if (!termino.isAfter(inicio)) {
			throw new RegraDeNegocioException("RN03", "termino", "Termino deve ser posterior ao inicio");
		}
	}

}
