package br.com.updev.domain;

import br.com.updev.dto.UsuarioRegister;
import jakarta.persistence.*;
import org.hibernate.envers.Audited;

@Audited
@Entity @Table(name="usuario")
public class Usuario extends BaseDomain {
	
	@Column(name="nome", nullable=false, unique=false, length=128)
	private String nome;
	
	@Column(name="email", nullable=false, unique=true, length=128)
	private String email;
	
	@Column(name="ativo", nullable=false)
	private boolean ativo = false;
	
	@Column(name="hash_senha", nullable=false, length=128)
	private String hashSenha;
	
	@ManyToOne @JoinColumn(name="perfil_id", nullable=false)
	private Perfil perfil;

	public Usuario(UsuarioRegister usuarioRegister, String senha, Perfil perfil) {
		super();
		this.nome = usuarioRegister.getNome();
		this.email = usuarioRegister.getUsername();
		this.ativo = true;
		this.hashSenha = senha;
		this.perfil = perfil;
	}

	public Usuario() {
		// Construtor padrão
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	public String getHashSenha() {
		return hashSenha;
	}

	public void setHashSenha(String hashSenha) {
		this.hashSenha = hashSenha;
	}

	public Perfil getPerfil() {
		return perfil;
	}

	public void setPerfil(Perfil perfil) {
		this.perfil = perfil;
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
