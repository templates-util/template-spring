package br.com.updev;

import br.com.updev.domain.Perfil;
import br.com.updev.domain.Permissao;
import br.com.updev.dto.Autorizacao;
import br.com.updev.dto.Credenciais;
import br.com.updev.dto.UsuarioDTO;
import br.com.updev.dto.UsuarioRequest;
import br.com.updev.repositories.PerfilRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public abstract class BaseTest {

	@Autowired
	protected WebTestClient httpClient;
	
    @Autowired
    protected PerfilRepository perfilRepository;

	private Random rand;

	@BeforeEach
	public void beforeEach() {
		this.httpClient = this.httpClient.mutate().responseTimeout(Duration.ofHours(4)).build();
	}
	
	protected String uuid() {
		return UUID.randomUUID().toString();
	}
	
    protected Perfil getPerfilAdmin() {
        return perfilRepository.findByNome("Administrador");
    }
	
    protected UsuarioRequest createUsuarioRequest(){
        UsuarioRequest request = new UsuarioRequest();
        request.setUsername(uuid());
        request.setPassword(uuid());
        request.setNome(uuid());
        request.setProfile(getPerfilAdmin().getUuid());
        request.setAtivo(true);
        return request;
    }
    
    protected UsuarioDTO criarUsuario(Perfil perfil, String email, String senha, boolean ativo){
        UsuarioRequest request = new UsuarioRequest();

        request.setUsername(email);
        request.setNome("Nome " + uuid());
        request.setPassword(senha);
        request.setProfile(perfil.getUuid());
        request.setAtivo(ativo);

        Autorizacao authAdmin = this.authAdmin();

        return this.httpClient.post().uri("/api/v1/user")
                .header("Authorization", authAdmin.getToken())
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(201)
                .expectBody(UsuarioDTO.class).returnResult().getResponseBody();
    }
    
    protected UsuarioDTO criarUsuario(Perfil perfil, String email, String senha){
       return criarUsuario(perfil, email, senha, true);
    }
    
    protected Perfil criarPerfil(List<Permissao> permissoes) {
    	Perfil perfil = new Perfil();
    	perfil.setAtivo(true);
    	perfil.setNome(uuid());
    	perfil.setPermissoes(new HashSet<>());
    	if (permissoes != null) {
    		for (Permissao permissao: permissoes) {
    			perfil.getPermissoes().add(permissao);
    		}
    	}
    	return perfilRepository.save(perfil);
    }

	protected Autorizacao authAdmin() {
		return this.autenticar("admin@itexto.com.br", "admin1234");
	}

	protected Autorizacao autenticar(String usuario, String senha) {
		Credenciais credenciais = new Credenciais(usuario, senha);
		return this.httpClient.post().uri("/api/v1/auth")
				.bodyValue(credenciais)
				.exchange()
				.expectBody(Autorizacao.class).returnResult().getResponseBody();
	}

	protected Random rand() {
		return rand == null ? new SecureRandom() : rand;
	}
	
}