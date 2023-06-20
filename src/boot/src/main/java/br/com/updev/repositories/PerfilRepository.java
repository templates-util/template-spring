package br.com.updev.repositories;

import br.com.updev.domain.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {
	
	Perfil findByNome(String nome);

	Optional<Perfil> findByUuid(String uuid);

}
