package br.com.updev.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionEntity;

@RevisionEntity(ListenerRevisao.class)
@Entity
@Table(name = "revisao")
public class Revisao extends DefaultRevisionEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7718406082044656673L;
	@ManyToOne
	@JoinColumn(name = "usuario_id", nullable = true)
	private Usuario usuario;

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
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
