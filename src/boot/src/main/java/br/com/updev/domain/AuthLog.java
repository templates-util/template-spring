package br.com.updev.domain;

import jakarta.persistence.*;

import java.util.Date;
import java.util.Objects;

@Entity @Table(name="auth_log")
public class AuthLog {
	
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	private long id;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="momento", nullable=false)
	private Date momento;
	
	@ManyToOne @JoinColumn(name="usuario_id")
	private Usuario usuario;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Date getMomento() {
		return momento;
	}

	public void setMomento(Date momento) {
		this.momento = momento;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
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
