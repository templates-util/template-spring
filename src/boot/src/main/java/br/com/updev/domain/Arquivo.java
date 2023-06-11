package br.com.updev.domain;

import jakarta.persistence.*;
import org.hibernate.envers.Audited;

import java.util.Objects;

@Entity
@Audited
@Table(name="arquivo")
public class Arquivo extends BaseDomain {
	
	@Column(name="bucket", nullable=false, length=64)
	private String bucket;
	
	@Column(name="original_name", nullable=false, length=128)
	private String originalName;
	
	@ManyToOne @JoinColumn(name="usuario_id", nullable=false)
	private Usuario usuario;
	
	@Column(name="excluido", nullable=false)
	private boolean excluido = false;

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
	}

	public String getOriginalName() {
		return originalName;
	}

	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public boolean isExcluido() {
		return excluido;
	}

	public void setExcluido(boolean excluido) {
		this.excluido = excluido;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		return super.equals(o);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode());
	}
}
