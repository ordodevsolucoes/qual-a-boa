package br.com.ordodev.qualaboa.erro;

import java.util.List;
import java.util.Map;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.com.ordodev.qualaboa.excecao.AcessoNegadoException;
import br.com.ordodev.qualaboa.excecao.RegraDeNegocioException;

// ProblemDetail (RFC 9457) para todo erro da API; nenhum handler aqui pode devolver
// stacktrace, nome de classe Java, SQL ou mensagem de framework no corpo da resposta
@RestControllerAdvice
public class ManipuladorDeExcecoes {

	@ExceptionHandler(RegraDeNegocioException.class)
	public ProblemDetail tratarRegraDeNegocio(RegraDeNegocioException excecao) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, excecao.getMessage());
		problema.setProperty("regra", excecao.getRegra());
		if (excecao.getCampo() != null) {
			problema.setProperty("campo", excecao.getCampo());
		}
		return problema;
	}

	@ExceptionHandler(AcessoNegadoException.class)
	public ProblemDetail tratarAcessoNegado(AcessoNegadoException excecao) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, excecao.getMessage());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ProblemDetail tratarCampoInvalido(MethodArgumentNotValidException excecao) {
		ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Um ou mais campos sao invalidos");
		List<Map<String, String>> erros = excecao.getBindingResult().getFieldErrors().stream()
				.map(erro -> Map.of("campo", erro.getField(), "mensagem", erro.getDefaultMessage()))
				.toList();
		problema.setProperty("errors", erros);
		return problema;
	}

	@ExceptionHandler(OptimisticLockingFailureException.class)
	public ProblemDetail tratarConflitoDeConcorrencia(OptimisticLockingFailureException excecao) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "O recurso foi alterado por outra requisicao, tente novamente");
	}

	// catch-all: qualquer exceção nao mapeada (JDBC, JPA, NPE) vira 500 generico,
	// mensagem nunca vem de excecao.getMessage() para nao repassar detalhe de framework
	@ExceptionHandler(Exception.class)
	public ProblemDetail tratarErroInesperado(Exception excecao) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado");
	}

}
