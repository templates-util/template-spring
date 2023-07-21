package br.com.updev.controllers;

import br.com.updev.dto.Autorizacao;
import br.com.updev.dto.Credenciais;
import br.com.updev.dto.UsuarioRegister;
import br.com.updev.services.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
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
	
	private final SecurityService securityService;

	@Autowired
	public AuthController(SecurityService securityService) {
		this.securityService = securityService;
	}

	@Operation(summary = "Authenticate a user")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully authenticated"),
			@ApiResponse(responseCode = "401", description = "Unauthorized")
	})
	@PostMapping("/api/v1/auth")
	public ResponseEntity<Autorizacao> autenticar(@RequestBody Credenciais credenciais) {
		
		Autorizacao auth = securityService.autenticar(credenciais);
		if (auth != null) {
			return new ResponseEntity<>(auth, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	@Operation(summary = "Access test for ROLE_ADMIN")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully accessed")
	})
	@Secured("ROLE_ADMIN")
	@GetMapping("/api/v1/test")
	public ResponseEntity<String> testeAcesso() {
		return new ResponseEntity<>("Ok", HttpStatus.OK);
	}

	@Operation(summary = "Register a new user")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Successfully created a new user"),
	})
	@PostMapping("/api/v1/register")
	public ResponseEntity<Void> cadastrarUsuario(@RequestBody @Valid UsuarioRegister usuarioRegister) {
		securityService.cadastrarUsuario(usuarioRegister);
		return new ResponseEntity<>(HttpStatus.CREATED);
    }

	@Operation(summary = "Home endpoint")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully accessed")
	})
	@GetMapping("/")
	public ResponseEntity<String> home() {
		return new ResponseEntity<>("Ok", HttpStatus.OK);
	}
}
