package br.com.updev.domain;

import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;


@MappedSuperclass
public abstract class BaseDomain implements java.io.Serializable {
	
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="id")
	private long id;
	
	@Column(name="uuid", length=64, unique=true, nullable=false, updatable=false)
	private String uuid;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="created_at", nullable=false, updatable=false)
	private Date createdAt;
	
	@PrePersist
	public void prePersist() {
		if (this.uuid == null) {
			this.uuid = UUID.randomUUID().toString();
		}
		if (this.createdAt == null) {
			this.createdAt = new Date();
		}
	}

	public boolean equals(Object obj) {
		return (obj == this) ||
				(obj instanceof BaseDomain baseDomain && obj.getClass().equals(getClass()) && (getId() == baseDomain.getId()));
	}
	
	public String toString() {
		return getClass().getName() + ":" + getId();
	}
	
	public int hashCode() {
		return toString().hashCode();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	
	
}
