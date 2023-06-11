package br.com.updev.services;

import br.com.updev.domain.Perfil;
import br.com.updev.domain.Usuario;
import br.com.updev.dto.BuscaUsuario;
import br.com.updev.dto.UsuarioRequest;
import br.com.updev.exceptions.NotFoundError;
import br.com.updev.exceptions.ServiceError;
import br.com.updev.repositories.PerfilRepository;
import br.com.updev.repositories.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UsuarioService {
	
	private final UsuarioRepository usuarioRepository;
	private final PerfilRepository perfilRepository;
	private final PasswordEncoder encoder;
	private final EntityManager entityManager;
	
	@Autowired
	public UsuarioService(UsuarioRepository usuarioRepository, PerfilRepository perfilRepository, PasswordEncoder encoder, EntityManager entityManager) {
		this.usuarioRepository = usuarioRepository;
		this.perfilRepository = perfilRepository;
		this.encoder = encoder;
		this.entityManager = entityManager;
	}
	
	private Perfil getPerfil(UsuarioRequest request) {
		Perfil perfil = perfilRepository.findByUuid(request.getProfile());
		if(perfil == null) {
			throw new ServiceError("Perfil desconhecido");
		}
		return perfil;
	}
	
	private Usuario findByUuid(String uuid) throws NotFoundError {
		Usuario usuario = usuarioRepository.findByUuid(uuid);
		if(usuario == null) {
			throw new NotFoundError("Usuário não encontrado.");
		}
		return usuario;
	}
	
	public Usuario create(UsuarioRequest request) {
		if(request.getPassword() == null || request.getPassword().isBlank()) {
			throw new ServiceError("Senha indefinida");
		}
		
		Usuario existente = usuarioRepository.findByEmail(request.getUsername());
		if(existente != null) {
			throw new ServiceError("Já existe um usuário cadastrado com este e-mail");
		}
		
		Perfil perfil = getPerfil(request);
		
		Usuario novoUsuario = new Usuario();
		novoUsuario.setPerfil(perfil);
		novoUsuario.setEmail(request.getUsername());
		novoUsuario.setNome(request.getNome());
		novoUsuario.setAtivo(request.isAtivo());
		novoUsuario.setHashSenha(this.encoder.encode(request.getPassword()));
		
		return usuarioRepository.saveAndFlush(novoUsuario);
	}
	
	public Usuario update(String uuid, UsuarioRequest request) throws NotFoundError {
		Usuario registro = findByUuid(uuid);
		
		Usuario outroRegistro = usuarioRepository.findByEmail(request.getUsername());
		if(outroRegistro != null && !outroRegistro.equals(registro)) {
			throw new ServiceError("Já existe outro usuário cadastrado com este e-mail");
		}
		
		Perfil perfil = getPerfil(request);
		
		registro.setPerfil(perfil);
		registro.setEmail(request.getUsername());
		registro.setNome(request.getNome());
		registro.setAtivo(request.isAtivo());
		if(request.getPassword() != null) {
			registro.setHashSenha(encoder.encode(request.getPassword()));
		}
		
		return usuarioRepository.saveAndFlush(registro);
	}
	
	public List<Usuario> getAll(BuscaUsuario busca) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Usuario> criteriaQuery = builder.createQuery(Usuario.class);
		Root<Usuario> root = criteriaQuery.from(Usuario.class);
		
		List<Predicate> predicates = new ArrayList<>();
		
		if(busca.getNome() != null) {
			predicates.add(builder.like(builder.lower(root.get("nome")), "%" + busca.getNome().toLowerCase() + "%"));
		}
		
		if(busca.getEmail() != null) {
			predicates.add(builder.like(builder.lower(root.get("email")), "%" + busca.getEmail().toLowerCase() + "%"));
		}

		if(!predicates.isEmpty()) {
			criteriaQuery.where(predicates.toArray(new Predicate[]{}));
		}
		criteriaQuery.orderBy(builder.asc(root.get("nome")));
		
		TypedQuery<Usuario> query = entityManager.createQuery(criteriaQuery);
		query.setMaxResults(busca.getPageSize());
        query.setFirstResult(busca.getPageSize() * busca.getPage());
        
		return query.getResultList();
	}
}
