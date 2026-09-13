package br.com.ordodev.qualaboa.seguranca;

import java.util.UUID;

import br.com.ordodev.qualaboa.usuario.PapelUsuario;

// principal da autenticacao: so o necessario para autorizacao no servico, sem carregar a entidade Usuario
public record UsuarioAutenticado(UUID id, PapelUsuario papel) {
}
