package br.com.updev.controllers;

import br.com.updev.dto.BuscaUsuario;
import br.com.updev.dto.UsuarioDTO;
import br.com.updev.dto.UsuarioRequest;
import br.com.updev.exceptions.NotFoundError;
import br.com.updev.exceptions.ServiceError;
import br.com.updev.services.UsuarioService;
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
public class UsuarioController {
	
	private final UsuarioService usuarioService;
	
	@Autowired
	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}
	
	@PostMapping()
	public ResponseEntity<UsuarioDTO> create(@RequestBody @Valid UsuarioRequest request) {
		return new ResponseEntity<>(new UsuarioDTO(usuarioService.create(request)), HttpStatus.CREATED);
	}
	
	@PutMapping("/{uuid}")
	public ResponseEntity<UsuarioDTO> update(@PathVariable("uuid") String uuid, @Valid @RequestBody UsuarioRequest request) throws NotFoundError {
		return new ResponseEntity<>(new UsuarioDTO(usuarioService.update(uuid, request)), HttpStatus.OK);
	}
	
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