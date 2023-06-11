package br.com.updev.domain;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;

import java.util.Set;

@Audited
@Entity @Table(name="perfil")
public class Perfil extends BaseDomain {
	
	@Column(name="nome", nullable=false, unique=true, length=128)
	private String nome;
	
	@Column(name="ativo", nullable=false)
	private boolean ativo = true;
	
	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	@ManyToMany(fetch=FetchType.EAGER) 
	@JoinTable(
			uniqueConstraints=@UniqueConstraint(columnNames={"perfil_id","permissoes_id"})
	)
	private Set<Permissao> permissoes;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Set<Permissao> getPermissoes() {
		return permissoes;
	}

	public void setPermissoes(Set<Permissao> permissoes) {
		this.permissoes = permissoes;
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
