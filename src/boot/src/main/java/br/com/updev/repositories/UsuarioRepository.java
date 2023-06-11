package br.com.updev.repositories;

import br.com.updev.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>{
	
	Usuario findByEmail(String email);
	
	Usuario findByUuid(String uuid);

}
