package br.com.updev.repositories;

import br.com.updev.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>{
	
	Optional<Usuario> findByEmail(String email);

	Optional<Usuario> findByUuid(String uuid);

	Optional<Usuario> findByEmailAndUuidNot(String email, String uuid);

}
