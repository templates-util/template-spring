package br.com.updev.security;

import br.com.updev.services.SecurityService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Component
public class JWTFilter extends GenericFilterBean implements ApplicationContextAware {

	private static final String TOKEN = "Authorization";
	private static final String API_KEY_HEADER = "x-api-key";
	private ApplicationContext applicationContext;
	
	private SecurityService getSecurityService() {
		return applicationContext.getBean(SecurityService.class);
	}
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String authHeader = httpRequest.getHeader(TOKEN);
		String apiKey = httpRequest.getHeader(API_KEY_HEADER);

		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			String token = authHeader.substring(7);
			JWTAuthentication auth = getSecurityService().parseToken(token);

			if (auth != null && auth.isAuthenticated()) {
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
		}
//		else if (apiKey != null) {
//			ApiKeyAuthentication auth = getSecurityService().authenticateApiKey(apiKey);
//			if (auth != null) {
//				SecurityContextHolder.getContext().setAuthentication(auth);
//			}
//		}


		chain.doFilter(request, response);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
}
