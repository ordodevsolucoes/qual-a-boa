package br.com.ordodev.qualaboa.evento;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "lote_ingresso")
public class LoteIngresso {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "evento_id", nullable = false)
	private Evento evento;

	@Column(nullable = false)
	private String nome;

	@Column(nullable = false)
	private Integer quantidade;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal preco;

	@Column(name = "vigencia_inicio", nullable = false)
	private Instant vigenciaInicio;

	@Column(name = "vigencia_fim", nullable = false)
	private Instant vigenciaFim;

	@Version
	@Column(nullable = false)
	private Integer versao;

	@Column(name = "criado_em", nullable = false, updatable = false)
	private Instant criadoEm;

	protected LoteIngresso() {
	}

	public LoteIngresso(Evento evento, String nome, Integer quantidade, BigDecimal preco, Instant vigenciaInicio,
			Instant vigenciaFim) {
		this.evento = evento;
		this.nome = nome;
		this.quantidade = quantidade;
		this.preco = preco;
		this.vigenciaInicio = vigenciaInicio;
		this.vigenciaFim = vigenciaFim;
	}

	void registrarCriacao(Instant instante) {
		this.criadoEm = instante;
	}

	public UUID getId() {
		return id;
	}

	public Evento getEvento() {
		return evento;
	}

	public String getNome() {
		return nome;
	}

	public Integer getQuantidade() {
		return quantidade;
	}

	public BigDecimal getPreco() {
		return preco;
	}

	public Instant getVigenciaInicio() {
		return vigenciaInicio;
	}

	public Instant getVigenciaFim() {
		return vigenciaFim;
	}

	public Integer getVersao() {
		return versao;
	}

	public Instant getCriadoEm() {
		return criadoEm;
	}

}
