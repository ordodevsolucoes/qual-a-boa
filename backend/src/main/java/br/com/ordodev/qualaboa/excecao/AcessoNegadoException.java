package br.com.ordodev.qualaboa.excecao;

// separada de RegraDeNegocioException porque o advice mapeia para 403, nao para 422:
// nao e uma violacao de regra de negocio, e uma tentativa de acesso fora da titularidade
public class AcessoNegadoException extends RuntimeException {

	public AcessoNegadoException(String mensagem) {
		super(mensagem);
	}

}
