package br.com.updev.services;

import br.com.updev.domain.Perfil;
import br.com.updev.domain.Permissao;
import br.com.updev.dto.PerfilEdit;
import br.com.updev.exceptions.NotFoundError;
import br.com.updev.exceptions.ServiceError;
import br.com.updev.repositories.PerfilRepository;
import br.com.updev.repositories.PermissaoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;

@Component
public class PerfilService {
	
	
	private final EntityManager entityManager;
	
	private final PerfilRepository perfilRepository;
	
	private final PermissaoRepository permissaoRepository;
	
	@Autowired
	public PerfilService(EntityManager entityManager, PerfilRepository perfilRepository, PermissaoRepository permissaoRepository) {
		this.entityManager = entityManager;
		this.perfilRepository = perfilRepository;
		this.permissaoRepository = permissaoRepository;
	}
	
	private static final Logger logger = LoggerFactory.getLogger("PerfilService");
	
	public Perfil findByUuid(String uuid) throws NotFoundError {
		Perfil result = perfilRepository.findByUuid(uuid);
		if (result == null) {
			throw new NotFoundError("Perfil não encontrado: " + uuid);
		}
		return result;
	}
	
	public Perfil update(String uuid, PerfilEdit dto) throws NotFoundError {

		logger.info("Update do perfil: {}", uuid);
		
		Perfil perfil = findByUuid(uuid);
		
		if (dto.getName() != null) {
			Perfil registro = perfilRepository.findByNome(dto.getName());
			if (registro.getId() != perfil.getId()) {
				throw new ServiceError("Já existe um outro perfil cadastrado com este nome");
			}
			perfil.setNome(dto.getName());
		}
		
		if (dto.getActive() != null) {
			perfil.setAtivo(dto.getActive());
		}
		
		if (dto.getRoles() != null) {
			perfil.setPermissoes(new HashSet<>());
			for (String role : dto.getRoles()) {
				Permissao permissao = permissaoRepository.findByAuthority(role);
				if (permissao == null) {
					throw new ServiceError("Permissão desconhecida: " + role);
				}
				perfil.getPermissoes().add(permissao);
			}
		}
		
		return perfilRepository.save(perfil);
	}
	
	public Perfil create(PerfilEdit dto) {
		
		if (dto.getName() == null) {
			throw new ServiceError("Nenhum nome fornecido");
		}
		
		Perfil outroRegistro = perfilRepository.findByNome(dto.getName());
		if (outroRegistro != null) {
			throw new ServiceError("Já existe um perfil com este nome cadastrado");
		}
		
		Perfil perfil = new Perfil();
		perfil.setNome(dto.getName());
		perfil.setAtivo(dto.getActive());
		perfil.setPermissoes(new HashSet<>());
		if (dto.getRoles() != null && ! dto.getRoles().isEmpty()) {
			for (String role : dto.getRoles()) {
				Permissao permissao = permissaoRepository.findByAuthority(role);
				if (permissao == null) {
					throw new ServiceError("Permissão desconhecida: " + role);
				}
				perfil.getPermissoes().add(permissao);
			}
		}
		
		return perfilRepository.save(perfil);
	}
	
	public List<Perfil> find(String name, Boolean active, Long pageSize, Long page)  {

		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Perfil> query = builder.createQuery(Perfil.class);

		Root<Perfil> root = query.from(Perfil.class);
		query.select(root);

		if (name != null && name.trim().length() > 0) {
			Predicate pName = builder.like(root.get("nome"), "%" + name + "%");
			query = query.where(pName);
		}

		if (active != null) {
			Predicate pActive = builder.equal(root.get("ativo"), active);
			query = query.where(pActive);
		}

		query.orderBy(builder.asc(root.get("nome")));
		TypedQuery<Perfil> busca = entityManager.createQuery(query);
		busca.setMaxResults(pageSize != null ?  pageSize.intValue() : 50);
		int pagina = page != null ?  page.intValue() : 0;
		busca.setFirstResult(pagina > 0 ? (int) (page * busca.getMaxResults()) : 0);

		return busca.getResultList();
	}

}
