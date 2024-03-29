package br.com.updev.services;

import br.com.updev.domain.AuthLog;
import br.com.updev.domain.Perfil;
import br.com.updev.domain.Permissao;
import br.com.updev.domain.Usuario;
import br.com.updev.dto.Autorizacao;
import br.com.updev.dto.Credenciais;
import br.com.updev.dto.UsuarioRegister;
import br.com.updev.exceptions.ServiceError;
import br.com.updev.exceptions.UnauthorizedException;
import br.com.updev.repositories.AuthLogRepository;
import br.com.updev.repositories.PerfilRepository;
import br.com.updev.repositories.PermissaoRepository;
import br.com.updev.repositories.UsuarioRepository;
import br.com.updev.security.ApiKeyAuthentication;
import br.com.updev.security.JWTAuthentication;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SecurityService {
	
	private final PermissaoRepository permissaoRepository;
	
	private final PerfilRepository perfilRepository;

	private final UsuarioRepository usuarioRepository;

	private final PasswordEncoder encoder;
	
	private final ConfigService configService;
	
	private final AuthLogRepository logRepository;

	private final JwtService jwtService;

	private final UsuarioService usuarioService;
	
	private static final Logger logger = LoggerFactory.getLogger("SecurityService");

	private static final String ADMINISTRADOR = "Administrador";

	private static final String USUARIO = "Usuario";

	@Value("${app.apikey}")
	private String apikey;

	@Autowired
	public SecurityService(PermissaoRepository permissaoRepository, PerfilRepository perfilRepository, UsuarioRepository usuarioRepository, PasswordEncoder encoder, ConfigService configService, AuthLogRepository logRepository, JwtService jwtService, UsuarioService usuarioService) {
		this.permissaoRepository = permissaoRepository;
		this.perfilRepository = perfilRepository;
		this.usuarioRepository = usuarioRepository;
		this.encoder = encoder;
		this.configService = configService;
		this.logRepository = logRepository;
		this.jwtService = jwtService;
		this.usuarioService = usuarioService;
	}


	public JWTAuthentication parseToken(String token) {

		try {
			Claims body = jwtService.parseToken(token);
			String email = body.getSubject();
			Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(email);
			if(usuarioOptional.isPresent()){
				Usuario usuario = usuarioOptional.get();
				if (usuario.isAtivo()) {
					return new JWTAuthentication(usuario);
				}
			}

		} catch (Exception t) {
			// erro parseando token
			logger.error("Erro validando token. Token inválido.", t);
		}
		
		return null;
	}



	public ApiKeyAuthentication authenticateApiKey(String apiKey) {
		if (apiKey != null && apiKey.equals(this.apikey)) {
			return new ApiKeyAuthentication(null, null);
		} else {
			return null;
		}
	}


	public Autorizacao autenticar(Credenciais credenciais) {
		Objects.requireNonNull(credenciais, "As credenciais não devem ser nulas");

		return usuarioRepository.findByEmail(credenciais.getUsername())
				.filter(usuario -> usuario.isAtivo() && this.checkSenha(credenciais.getPassword(), usuario.getHashSenha()))
				.map(usuario -> {
					Date agora = new Date();
					Autorizacao resultado = new Autorizacao();
					resultado.setDateCreated(agora.getTime());
					resultado.setName(usuario.getNome());
					resultado.setToken(this.criarToken(usuario, agora.getTime()));
					resultado.setTimeToLive(Long.parseLong(this.configService.info("jwt.ttl")));
					resultado.setUsername(usuario.getEmail());
					resultado.setRoles(usuario.getPerfil().getPermissoes().stream().map(Permissao::getAuthority).toList());

					AuthLog log = new AuthLog();
					log.setUsuario(usuario);
					log.setMomento(agora);
					this.logRepository.save(log);

					return resultado;
				})
				.orElseThrow(() -> new UnauthorizedException("Falha na autenticação"));
	}

	
	private String criarToken(Usuario usuario, long agora) {
		return jwtService.criarToken(usuario.getEmail(), agora);
	}
	
	private boolean checkSenha(String raw, String hash) {
		return encoder.matches(raw, hash);
	}
	
	private String hashSenha(String raw) {
		return this.encoder.encode(raw);
	}
	
	@PostConstruct
	public void init() {
		SecretKey key = Keys.hmacShaKeyFor(configService.info("jwt.secret").getBytes(StandardCharsets.UTF_8));

		try (InputStream input = getClass().getClassLoader().getResourceAsStream("roles.properties")) {
			Properties props = new Properties();
			props.load(input);
			Set<String> existingAuthorities = permissaoRepository.findAll()
					.stream()
					.map(Permissao::getAuthority)
					.collect(Collectors.toSet());

			for (Map.Entry<Object, Object> entry : props.entrySet()) {
				String chave = entry.getKey().toString();
				if (!existingAuthorities.contains(chave)) {
					Permissao permissao = new Permissao();
					permissao.setAuthority(chave);
					permissao.setNome(entry.getValue().toString());
					permissaoRepository.save(permissao);
				}
			}

		} catch (IOException ex) {
			throw new ServiceError("Erro carregando permissões padrão", ex);
		}
		
		Perfil perfil = perfilRepository.findByNome(ADMINISTRADOR);
		if (perfil == null) {
			perfil = new Perfil();
			perfil.setNome(ADMINISTRADOR);
			perfil.setPermissoes(new HashSet<>());
			perfil.setAtivo(true);
			List<Permissao> permissoes = permissaoRepository.findAll();
			perfil.getPermissoes().addAll(permissoes);
			perfilRepository.save(perfil);
		}else{
			perfil.setPermissoes(new HashSet<>());
			List<Permissao> permissoes = permissaoRepository.findAll();
			perfil.getPermissoes().addAll(permissoes);
			perfilRepository.save(perfil);
		}

		Perfil perfilUser = perfilRepository.findByNome(USUARIO);
		if (perfilUser == null) {
			perfilUser = new Perfil();
			perfilUser.setNome(USUARIO);
			perfilUser.setPermissoes(new HashSet<>());
			perfilUser.setAtivo(true);
			perfilUser.getPermissoes().add(permissaoRepository.findByAuthority("ROLE_USER"));
			perfilRepository.save(perfilUser);
		}
		
		Optional<Usuario> admin = usuarioRepository.findByEmail("admin@updev.com.br");

		if (admin.isEmpty()) {
			Usuario usuario = new Usuario();
			usuario.setNome(ADMINISTRADOR);
			usuario.setEmail("admin@updev.com.br");
			usuario.setAtivo(true);
			usuario.setPerfil(perfil);
			usuario.setHashSenha(this.hashSenha("admin1234"));
			usuarioRepository.save(usuario);
		}
		
	}

	public void cadastrarUsuario(UsuarioRegister usuarioRegister) {
		Objects.requireNonNull(usuarioRegister, "O usuário não pode ser nulo");
        Objects.requireNonNull(usuarioRegister.getNome(), "O nome do usuário não pode ser nulo");
        Objects.requireNonNull(usuarioRegister.getUsername(), "O email do usuário não pode ser nulo");
        Objects.requireNonNull(usuarioRegister.getSenha(), "A senha do usuário não pode ser nula");

        usuarioService.findByEmailValidateEmail(usuarioRegister.getUsername());

		usuarioRepository.save(new Usuario(usuarioRegister, this.hashSenha(usuarioRegister.getSenha()), perfilRepository.findByNome(USUARIO)));
	}
}
