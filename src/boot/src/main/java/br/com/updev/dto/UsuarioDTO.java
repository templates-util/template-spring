package br.com.updev.dto;

import br.com.updev.UtilData;
import br.com.updev.domain.Usuario;

public class UsuarioDTO extends UsuarioRequest {
	
	private String uuid;
	private String dateCreated;
	
	public UsuarioDTO() {
		//construtor padrão
	}
	
	public UsuarioDTO(Usuario usuario) {
		super(usuario);
		this.uuid = usuario.getUuid();
		this.dateCreated = UtilData.formatoDataBr().format(usuario.getCreatedAt());
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}

}