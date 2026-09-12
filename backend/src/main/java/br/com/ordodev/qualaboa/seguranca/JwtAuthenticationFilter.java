package br.com.ordodev.qualaboa.seguranca;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// instanciado direto dentro de ConfiguracaoSeguranca (nao e @Component) para nao ser
// registrado duas vezes: uma pela cadeia do Spring Security, outra pelo auto-registro
// de Filter que o Spring Boot faz para todo bean desse tipo
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String PREFIXO_BEARER = "Bearer ";

	private final JwtService jwtService;

	public JwtAuthenticationFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String cabecalho = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (cabecalho != null && cabecalho.startsWith(PREFIXO_BEARER)) {
			autenticar(cabecalho.substring(PREFIXO_BEARER.length()));
		}
		filterChain.doFilter(request, response);
	}

	private void autenticar(String token) {
		try {
			UsuarioAutenticado usuarioAutenticado = jwtService.validar(token);
			List<SimpleGrantedAuthority> autoridades = List.of(new SimpleGrantedAuthority("ROLE_" + usuarioAutenticado.papel().name()));
			var autenticacao = new UsernamePasswordAuthenticationToken(usuarioAutenticado, null, autoridades);
			SecurityContextHolder.getContext().setAuthentication(autenticacao);
		} catch (JwtException | IllegalArgumentException tokenInvalido) {
			// token ausente/expirado/malformado: segue sem autenticar, o endpoint decide se exige 401
		}
	}

}
