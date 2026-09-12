package br.com.ordodev.qualaboa.excecao;

public class CredenciaisInvalidasException extends RuntimeException {

	public CredenciaisInvalidasException(String mensagem) {
		super(mensagem);
	}

}
