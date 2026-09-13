package br.com.ordodev.qualaboa.autenticacao;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.ordodev.qualaboa.excecao.CredenciaisInvalidasException;
import br.com.ordodev.qualaboa.seguranca.JwtService;
import br.com.ordodev.qualaboa.seguranca.UsuarioAutenticado;
import br.com.ordodev.qualaboa.usuario.Usuario;
import br.com.ordodev.qualaboa.usuario.UsuarioRepository;

@Service
public class AuthService {

	private static final String MENSAGEM_CREDENCIAIS_INVALIDAS = "Email ou senha invalidos";

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	// mesma excecao e mensagem para e-mail inexistente e para senha errada, de proposito,
	// para a resposta 401 nao revelar se o e-mail esta cadastrado
	public String autenticar(String email, String senha) {
		Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
		if (usuario == null || !passwordEncoder.matches(senha, usuario.getSenhaHash())) {
			throw new CredenciaisInvalidasException(MENSAGEM_CREDENCIAIS_INVALIDAS);
		}
		UsuarioAutenticado usuarioAutenticado = new UsuarioAutenticado(usuario.getId(), usuario.getPapel());
		return jwtService.gerar(usuarioAutenticado);
	}

}
