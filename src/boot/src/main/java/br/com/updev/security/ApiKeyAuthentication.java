package br.com.updev.security;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class ApiKeyAuthentication extends UsernamePasswordAuthenticationToken {

    public ApiKeyAuthentication(Object principal, Object credentials) {
        super(principal, credentials);
    }

    // Você pode adicionar mais campos e métodos conforme a necessidade
}