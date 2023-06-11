package br.com.updev.dto;

public class BuscaUsuario extends Busca {
	
	private String nome;
	private String email;
	
	public BuscaUsuario() {
		//construtor padrão
	}
	
	public BuscaUsuario(int pageSize, int page, String nome, String email) {
		super(pageSize, page);
		this.nome = nome;
		this.email = email;
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

}
