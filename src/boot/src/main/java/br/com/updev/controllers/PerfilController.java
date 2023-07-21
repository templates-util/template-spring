package br.com.updev.controllers;

import br.com.updev.domain.Perfil;
import br.com.updev.domain.Permissao;
import br.com.updev.dto.Erro;
import br.com.updev.dto.PerfilEdit;
import br.com.updev.dto.PerfilResponse;
import br.com.updev.dto.PermissaoResponse;
import br.com.updev.exceptions.NotFoundError;
import br.com.updev.repositories.PermissaoRepository;
import br.com.updev.services.PerfilService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@SecurityRequirement(name = "bearerAuth")
public class PerfilController {
	
	private final PerfilService perfilService;
	
	private final PermissaoRepository permissaoRepository;

	@Autowired
	public PerfilController(PerfilService perfilService, PermissaoRepository permissaoRepository) {
		this.perfilService = perfilService;
		this.permissaoRepository = permissaoRepository;
	}

	@Operation(summary = "Get all permissions")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully fetched permissions")
	})
	@GetMapping("/api/v1/roles")
	@Secured({"ROLE_ADMIN", "ROLE_PROFILE_CREATE", "ROLE_PROFILE_UPDATE"})
	public ResponseEntity<List<PermissaoResponse>> permissoes() {
		List<Permissao> result = permissaoRepository.findAll();
		List<PermissaoResponse> saida = new ArrayList<>();
		for (Permissao perm : result) {
			saida.add(new PermissaoResponse(perm));
		}
		return new ResponseEntity<>(saida, HttpStatus.OK);
	}
	@Operation(summary = "Get profile by ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully fetched profile"),
			@ApiResponse(responseCode = "404", description = "Profile not found", content = @Content(schema= @Schema(implementation = Erro.class)) )
	})
	@GetMapping("/api/v1/profile/{id}")
	@Secured({"ROLE_PROFILE_LIST", "ROLE_ADMIN"})
	public ResponseEntity<PerfilResponse> getOne(@PathVariable("id") String uuid) throws NotFoundError {
		Perfil perfil = perfilService.findByUuid(uuid);
		return new ResponseEntity<>(new PerfilResponse(perfil), HttpStatus.OK);
	}
	@Operation(summary = "Update a profile")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully updated profile"),
			@ApiResponse(responseCode = "404", description = "Profile not found", content = @Content(schema= @Schema(implementation = Erro.class)))
	})
	@PutMapping("/api/v1/profile/{id}")
	@Secured({"ROLE_PROFILE_UPDATE", "ROLE_ADMIN"})
	public ResponseEntity<PerfilResponse> update(@RequestBody PerfilEdit dto, @PathVariable("id") String uuid) throws NotFoundError {
		Perfil resultado = perfilService.update(uuid, dto);
		return new ResponseEntity<>(new PerfilResponse(resultado), HttpStatus.OK);
	}
	@Operation(summary = "List profiles")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully fetched profiles"),
	})
	@Secured({"ROLE_LIST_PROFILE", "ROLE_ADMIN"})
	@GetMapping("/api/v1/profile")
	public ResponseEntity<List<PerfilResponse>> list(
			@RequestParam(required=false, name="name") String name, 
			@RequestParam(required=false, name="active") Boolean active, 
			@RequestParam(required=false, name="pageSize") Long pageSize, 
			@RequestParam(required=false, name="page") Long page
		) {
		
		List<Perfil> perfis = perfilService.find(name, active, pageSize, page);
		List<PerfilResponse> result = new ArrayList<>();
		for (Perfil perfil : perfis) {
			result.add(new PerfilResponse(perfil));
		}
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@Operation(summary = "Create a new profile")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Successfully created a new profile"),
	})
	@Secured({"ROLE_CREATE_PROFILE", "ROLE_ADMIN"})
	@PostMapping("/api/v1/profile")
	public ResponseEntity<PerfilResponse> create(@RequestBody PerfilEdit dto) {
		Perfil perfil = perfilService.create(dto);
		return new ResponseEntity<>(new PerfilResponse(perfil), HttpStatus.CREATED);
	}

}
