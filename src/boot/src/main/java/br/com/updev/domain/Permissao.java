package br.com.updev.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.envers.Audited;
import org.springframework.security.core.GrantedAuthority;

@Audited
@Entity @Table(name="permissao")
public class Permissao extends BaseDomain implements GrantedAuthority {

	@Column(name="authority", length=128, unique=true, nullable=false)
	private String authority;
	
	@Column(name="nome", length=255, nullable=false)
	private String nome;

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}
	
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}

}
