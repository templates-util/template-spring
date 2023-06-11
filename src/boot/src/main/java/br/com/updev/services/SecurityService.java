package br.com.updev.services;

import br.com.updev.domain.AuthLog;
import br.com.updev.domain.Perfil;
import br.com.updev.domain.Permissao;
import br.com.updev.domain.Usuario;
import br.com.updev.dto.Autorizacao;
import br.com.updev.dto.Credenciais;
import br.com.updev.exceptions.ServiceError;
import br.com.updev.repositories.AuthLogRepository;
import br.com.updev.repositories.PerfilRepository;
import br.com.updev.repositories.PermissaoRepository;
import br.com.updev.repositories.UsuarioRepository;
import br.com.updev.security.JWTAuthentication;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	
	private static final Logger logger = LoggerFactory.getLogger("SecurityService");

	private static final String ADMINISTRADOR = "Administrador";

	private SecretKey key;

	@Autowired
	public SecurityService(PermissaoRepository permissaoRepository, PerfilRepository perfilRepository, UsuarioRepository usuarioRepository, PasswordEncoder encoder, ConfigService configService, AuthLogRepository logRepository) {
		this.permissaoRepository = permissaoRepository;
		this.perfilRepository = perfilRepository;
		this.usuarioRepository = usuarioRepository;
		this.encoder = encoder;
		this.configService = configService;
		this.logRepository = logRepository;
	}


	public JWTAuthentication parseToken(String token) {

		try {
			Claims body = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
			String email = body.getSubject();
			Usuario usuario = usuarioRepository.findByEmail(email);
			if (usuario != null && usuario.isAtivo()) {
				return new JWTAuthentication(usuario);
			}
		} catch (Exception t) {
			// erro parseando token
			logger.error("Erro validando token. Token inválido.", t);
		}
		
		return null;
	}
	
	
	public Autorizacao autenticar(Credenciais credenciais) {
		if (credenciais != null && credenciais.getUsername() != null && credenciais.getPassword() != null) {
			Usuario usuario = usuarioRepository.findByEmail(credenciais.getUsername());
			if (usuario != null && usuario.isAtivo() &&
				this.checkSenha(credenciais.getPassword(), usuario.getHashSenha())) {
				Date agora = new Date();
				Autorizacao resultado = new Autorizacao();
				resultado.setDateCreated(agora.getTime());
				resultado.setName(usuario.getNome());
				resultado.setToken(this.criarToken(usuario, agora.getTime()));
				resultado.setTimeToLive(Long.parseLong(this.configService.info("jwt.ttl")));
				resultado.setUsername(usuario.getEmail());
				resultado.setRoles(new ArrayList<>());
				for (Permissao perm : usuario.getPerfil().getPermissoes()) {
					resultado.getRoles().add(perm.getAuthority());
				}
				
				AuthLog log = new AuthLog();
				log.setUsuario(usuario);
				log.setMomento(agora);
				this.logRepository.save(log);
				
				return resultado;
			}
		}
		return null;
	}
	
	private String criarToken(Usuario usuario, long agora) {
		Claims claims = Jwts.claims().setSubject(usuario.getEmail());
		long timeToLive = Long.parseLong(configService.info("jwt.ttl"));
		return Jwts.builder()
				  .setClaims(claims)
				  .setExpiration(new Date(agora + timeToLive))
				  .signWith(key, SignatureAlgorithm.HS512)
				  .compact();
	}
	
	private boolean checkSenha(String raw, String hash) {
		return encoder.matches(raw, hash);
	}
	
	private String hashSenha(String raw) {
		return this.encoder.encode(raw);
	}
	
	@PostConstruct
	public void init() {
		key = Keys.hmacShaKeyFor(configService.info("jwt.secret").getBytes(StandardCharsets.UTF_8));

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
		}
		
		Usuario admin = usuarioRepository.findByEmail("admin@updev.com.br");
		if (admin == null) {
			admin = new Usuario();
			admin.setNome(ADMINISTRADOR);
			admin.setEmail("admin@updev.com.br");
			admin.setAtivo(true);
			admin.setPerfil(perfil);
			admin.setHashSenha(this.hashSenha("admin1234"));
			usuarioRepository.save(admin);
		}
		
	}

}
