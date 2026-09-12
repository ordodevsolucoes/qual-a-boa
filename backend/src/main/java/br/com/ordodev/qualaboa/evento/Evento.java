package br.com.ordodev.qualaboa.evento;

import java.time.Instant;
import java.util.UUID;

import br.com.ordodev.qualaboa.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "evento")
public class Evento {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "local_id", nullable = false)
	private Usuario local;

	@Column(nullable = false)
	private String titulo;

	@Column(nullable = false)
	private String descricao;

	@Column(name = "area_conhecimento", nullable = false)
	private String areaConhecimento;

	@Column(nullable = false)
	private Instant inicio;

	@Column(nullable = false)
	private Instant termino;

	@Column(nullable = false)
	private String logradouro;

	@Column(nullable = false)
	private String numero;

	@Column(nullable = false)
	private String bairro;

	@Column(nullable = false)
	private String cidade;

	// char(2)/char(8) no banco sao bpchar, tipo distinto do varchar padrao do Hibernate para String
	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(nullable = false, length = 2)
	private String uf;

	@JdbcTypeCode(SqlTypes.CHAR)
	@Column(nullable = false, length = 8)
	private String cep;

	@Column(name = "capacidade_total", nullable = false)
	private Integer capacidadeTotal;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private SituacaoEvento situacao = SituacaoEvento.RASCUNHO;

	@Version
	@Column(nullable = false)
	private Integer versao;

	@Column(name = "criado_em", nullable = false, updatable = false)
	private Instant criadoEm;

	@Column(name = "atualizado_em", nullable = false)
	private Instant atualizadoEm;

	protected Evento() {
	}

	public Evento(Usuario local, String titulo, String descricao, String areaConhecimento, Instant inicio,
			Instant termino, String logradouro, String numero, String bairro, String cidade, String uf, String cep,
			Integer capacidadeTotal) {
		this.local = local;
		this.titulo = titulo;
		this.descricao = descricao;
		this.areaConhecimento = areaConhecimento;
		this.inicio = inicio;
		this.termino = termino;
		this.logradouro = logradouro;
		this.numero = numero;
		this.bairro = bairro;
		this.cidade = cidade;
		this.uf = uf;
		this.cep = cep;
		this.capacidadeTotal = capacidadeTotal;
	}

	// so o service decide quando o evento nasce, por isso o metodo fica restrito ao pacote
	void registrarCriacao(Instant instante) {
		this.criadoEm = instante;
		this.atualizadoEm = instante;
	}

	void publicar(Instant instante) {
		this.situacao = SituacaoEvento.PUBLICADO;
		this.atualizadoEm = instante;
	}

	public UUID getId() {
		return id;
	}

	public Usuario getLocal() {
		return local;
	}

	public String getTitulo() {
		return titulo;
	}

	public String getDescricao() {
		return descricao;
	}

	public String getAreaConhecimento() {
		return areaConhecimento;
	}

	public Instant getInicio() {
		return inicio;
	}

	public Instant getTermino() {
		return termino;
	}

	public String getLogradouro() {
		return logradouro;
	}

	public String getNumero() {
		return numero;
	}

	public String getBairro() {
		return bairro;
	}

	public String getCidade() {
		return cidade;
	}

	public String getUf() {
		return uf;
	}

	public String getCep() {
		return cep;
	}

	public Integer getCapacidadeTotal() {
		return capacidadeTotal;
	}

	public SituacaoEvento getSituacao() {
		return situacao;
	}

	public Integer getVersao() {
		return versao;
	}

	public Instant getCriadoEm() {
		return criadoEm;
	}

	public Instant getAtualizadoEm() {
		return atualizadoEm;
	}

}
