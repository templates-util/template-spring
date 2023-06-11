package br.com.updev.repositories;

import br.com.updev.domain.Permissao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissaoRepository extends JpaRepository<Permissao, Long> {
	
	Permissao findByAuthority(String authority);

}
