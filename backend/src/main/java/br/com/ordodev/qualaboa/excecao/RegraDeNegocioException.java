package br.com.ordodev.qualaboa.excecao;

// exececao propria por RN em vez de IllegalArgumentException para o controller conseguir
// montar a resposta RFC 9457 com o codigo da regra e o campo violado, sem inspecionar mensagem
public class RegraDeNegocioException extends RuntimeException {

	private final String regra;
	private final String campo;

	public RegraDeNegocioException(String regra, String mensagem) {
		this(regra, null, mensagem);
	}

	public RegraDeNegocioException(String regra, String campo, String mensagem) {
		super(mensagem);
		this.regra = regra;
		this.campo = campo;
	}

	public String getRegra() {
		return regra;
	}

	public String getCampo() {
		return campo;
	}

}
