package br.com.ordodev.qualaboa.usuario;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(name = "senha_hash", nullable = false)
	private String senhaHash;

	@Column(nullable = false)
	private String nome;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PapelUsuario papel;

	@Column(name = "criado_em", nullable = false, updatable = false)
	private Instant criadoEm;

	protected Usuario() {
	}

	public Usuario(String email, String senhaHash, String nome, PapelUsuario papel, Instant criadoEm) {
		this.email = email;
		this.senhaHash = senhaHash;
		this.nome = nome;
		this.papel = papel;
		this.criadoEm = criadoEm;
	}

	public UUID getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getSenhaHash() {
		return senhaHash;
	}

	public String getNome() {
		return nome;
	}

	public PapelUsuario getPapel() {
		return papel;
	}

	public Instant getCriadoEm() {
		return criadoEm;
	}

}
