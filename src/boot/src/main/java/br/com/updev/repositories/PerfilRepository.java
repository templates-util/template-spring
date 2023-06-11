package br.com.updev.repositories;

import br.com.updev.domain.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {
	
	Perfil findByNome(String nome);
	
	Perfil findByUuid(String uuid);

}
