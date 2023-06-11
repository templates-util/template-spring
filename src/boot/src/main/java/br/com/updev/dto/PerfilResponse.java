package br.com.updev.dto;

import br.com.updev.domain.Perfil;
import br.com.updev.domain.Permissao;

import java.util.ArrayList;
import java.util.List;

public class PerfilResponse {
	
	private String uuid;
	
	private String name;
	
	private boolean active;
	
	public PerfilResponse() {
		// construtor padrão
	}
	
	public PerfilResponse(Perfil perfil) {
		this.uuid = perfil.getUuid();
		this.name = perfil.getNome();
		this.active = perfil.isAtivo();
		this.roles = new ArrayList<>();
		if (perfil.getPermissoes() != null) {
			for (Permissao perm : perfil.getPermissoes()) {
				this.roles.add(new PermissaoResponse(perm));
			}
		}
	}
	
	private List<PermissaoResponse> roles;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public List<PermissaoResponse> getRoles() {
		return roles;
	}

	public void setRoles(List<PermissaoResponse> roles) {
		this.roles = roles;
	}
	
	

}
