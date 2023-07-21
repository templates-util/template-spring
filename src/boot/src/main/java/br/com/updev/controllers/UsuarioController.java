package br.com.updev.controllers;

import br.com.updev.dto.BuscaUsuario;
import br.com.updev.dto.Erro;
import br.com.updev.dto.UsuarioDTO;
import br.com.updev.dto.UsuarioRequest;
import br.com.updev.exceptions.NotFoundError;
import br.com.updev.exceptions.ServiceError;
import br.com.updev.services.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Secured({"ROLE_ADMIN"})
@RestController
@RequestMapping("/api/v1/user")
@Validated
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {
	
	private final UsuarioService usuarioService;
	
	@Autowired
	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@Operation(summary = "Cria um novo usuário",
			responses = {
					@ApiResponse(responseCode = "201", description = "Usuário criado com sucesso",
							content = @Content(schema = @Schema(implementation = UsuarioDTO.class))),
					@ApiResponse(responseCode = "500", description = "Erro interno do servidor",
							content = @Content(schema = @Schema(implementation = Erro.class)))
			})
	@PostMapping()
	public ResponseEntity<UsuarioDTO> create(@RequestBody @Valid UsuarioRequest request) {
		return new ResponseEntity<>(new UsuarioDTO(usuarioService.create(request)), HttpStatus.CREATED);
	}

	@Operation(summary = "Atualiza um usuário existente",
			responses = {
					@ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso",
							content = @Content(schema = @Schema(implementation = UsuarioDTO.class))),
					@ApiResponse(responseCode = "404", description = "Usuário não encontrado",
							content = @Content(schema = @Schema(implementation = Erro.class))),
					@ApiResponse(responseCode = "500", description = "Erro interno do servidor",
							content = @Content(schema = @Schema(implementation = Erro.class)))
			})
	@PutMapping("/{uuid}")
	public ResponseEntity<UsuarioDTO> update(@PathVariable("uuid") String uuid, @Valid @RequestBody UsuarioRequest request) throws NotFoundError {
		return new ResponseEntity<>(new UsuarioDTO(usuarioService.update(uuid, request)), HttpStatus.OK);
	}

	@Operation(summary = "Obtém uma lista de todos os usuários",
			responses = {
					@ApiResponse(responseCode = "200", description = "Operação bem-sucedida",
							content = @Content(array = @ArraySchema(schema = @Schema(implementation = UsuarioDTO.class)))),
					@ApiResponse(responseCode = "400", description = "Parâmetros inválidos",
							content = @Content(schema = @Schema(implementation = Erro.class))),
					@ApiResponse(responseCode = "500", description = "Erro interno do servidor",
							content = @Content(schema = @Schema(implementation = Erro.class)))
			})
	@GetMapping()
	public ResponseEntity<List<UsuarioDTO>> getAll(
			@RequestParam(name="nome", required=false) String nome,
			@RequestParam(name="email", required=false) String email,
			@RequestParam(name="page", required=false, defaultValue = "0") int page,
			@RequestParam(name="pageSize", required=false, defaultValue = "50") int pageSize) {
		
        if(pageSize <= 0){
            throw new ServiceError("Parâmetro de tamanho de página inválido");
        }

        if(page < 0){
            throw new ServiceError("Parâmetro de página inválido");
        }
        
		BuscaUsuario busca = new BuscaUsuario(pageSize, page, nome, email);
		return ResponseEntity.ok().body(usuarioService.getAll(busca)
				.stream()
				.map(UsuarioDTO::new)
				.toList());
	}

}