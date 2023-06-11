package br.com.updev.controllers;

import br.com.updev.dto.Autorizacao;
import br.com.updev.dto.Credenciais;
import br.com.updev.services.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class AuthController {
	
	@Autowired
	private SecurityService securityService;
	
	
	@PostMapping("/api/v1/auth")
	public ResponseEntity<Autorizacao> autenticar(@RequestBody Credenciais credenciais) {
		
		Autorizacao auth = securityService.autenticar(credenciais);
		if (auth != null) {
			return new ResponseEntity<>(auth, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}
	
	@Secured("ROLE_ADMIN")
	@GetMapping("/api/v1/test")
	public ResponseEntity<String> testeAcesso() {
		return new ResponseEntity<>("Ok", HttpStatus.OK);
	}

}
