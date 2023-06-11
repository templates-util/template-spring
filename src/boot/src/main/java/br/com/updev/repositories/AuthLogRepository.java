package br.com.updev.repositories;

import br.com.updev.domain.AuthLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthLogRepository extends JpaRepository<AuthLog, Long> {

}
