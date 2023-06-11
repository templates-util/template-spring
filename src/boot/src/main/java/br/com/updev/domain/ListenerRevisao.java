package br.com.updev.domain;

import br.com.updev.security.JWTAuthentication;
import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class ListenerRevisao implements RevisionListener {

	@Override
	public void newRevision(Object revisionEntity) {
		
		Revisao revisao = (Revisao) revisionEntity;
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth instanceof JWTAuthentication jwtAuthentication) {
			revisao.setUsuario(jwtAuthentication.getUsuario());
		}
		
	}

}