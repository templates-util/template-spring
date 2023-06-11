package br.com.updev.dto;

import br.com.updev.domain.Permissao;

public class PermissaoResponse {
	
	private String authority;
	
	private String name;
	
	public PermissaoResponse() {
		// construtor padrão
	}
	
	public PermissaoResponse(Permissao permissao) {
		this.authority = permissao.getAuthority();
		this.name = permissao.getNome();
	}

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
