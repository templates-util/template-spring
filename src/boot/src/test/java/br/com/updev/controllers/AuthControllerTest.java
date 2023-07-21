package br.com.updev.controllers;

import br.com.updev.BaseTest;
import br.com.updev.domain.Permissao;
import br.com.updev.domain.Usuario;
import br.com.updev.dto.Autorizacao;
import br.com.updev.dto.Credenciais;
import br.com.updev.dto.Erro;
import br.com.updev.dto.UsuarioRegister;
import br.com.updev.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AuthControllerTest extends BaseTest {

    @Autowired
    private AuthController controller;

    @Autowired
    private WebTestClient testClient;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void testAutenticacaoBemSucedida() {

        Credenciais credenciais = new Credenciais("admin@updev.com.br", "admin1234");
        ResponseEntity<Autorizacao> result = controller.autenticar(credenciais);


        assertEquals(HttpStatus.OK, result.getStatusCode());
        Autorizacao autorizacao = result.getBody();
        assertNotNull(autorizacao, "Não retornou as autorizações");

        assertEquals(credenciais.getUsername(), autorizacao.getUsername(), "Retornou o username incorreto");
        assertNotNull(autorizacao.getToken(), "Não nos retornou um token");
        assertNotNull(autorizacao.getName(), "Não retornou o nome do usuário");

        Optional<Usuario> usuarioOptional = usuarioRepository.findByEmail(autorizacao.getUsername());
        assertTrue(usuarioOptional.isPresent(), "O registro do usuário não existe no banco de dados");
        Usuario usuario = usuarioOptional.get();
        assertNotNull(usuarioOptional, "O registro do usuário não existe no banco de dados");
        for (Permissao permissao : usuario.getPerfil().getPermissoes()) {
            boolean presente = autorizacao.getRoles().stream().anyMatch(p -> p.equals(permissao.getAuthority()));
            assertTrue(presente, "A permissão " + permissao.getAuthority() + " não foi encontrada");
        }


        /*
         * Se a autenticação está correta, consigo acessar um endpoint de teste
         */
        this.testClient.get().uri("/api/v1/test")
                .header("Authorization",String.format("Bearer %s", autorizacao.getToken()))
                .exchange().expectStatus().isOk();
        /*
         * Se fornecemos um token inválido, não devo conseguir acessar este conteúdo
         */
        this.testClient.get().uri("/api/v1/test")
                .header("Authorization", "invalido")
                .exchange().expectStatus().isUnauthorized();
    }

    @Test
    void testAutenticacaoMalSucedida() {
        Credenciais credenciais = new Credenciais(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        try{
            controller.autenticar(credenciais);

        }catch(Exception e){
            assertEquals("Falha na autenticação", e.getMessage());
        }
    }

    /**
     * POST /api/v1/register
     *
     * Criação do usuário com todas as condições satisfeitas
     */
    @Test
    void testRegisterUser(){
        UsuarioRegister usuarioRegister = new UsuarioRegister(uuid(), uuid(), uuid());

        this.testClient.post().uri("/api/v1/register")
                .bodyValue(usuarioRegister)
                .exchange().expectStatus().isCreated();
    }

    /**
     * POST /api/v1/register
     *
     * Criação do usuário com o  mesmo email
     *
     */
    @Test
    void testRegisterUserMesmoEmail(){
        UsuarioRegister usuarioRegister = new UsuarioRegister(uuid(), uuid(), uuid());

        this.testClient.post().uri("/api/v1/register")
                .bodyValue(usuarioRegister)
                .exchange().expectStatus().isCreated();

        Erro erro = this.testClient.post().uri("/api/v1/register")
                .bodyValue(usuarioRegister)
                .exchange().expectStatus().isBadRequest()
                .expectBody(Erro.class).returnResult().getResponseBody();

        assertNotNull(erro);
        assertEquals("Já existe um usuário cadastrado com este e-mail", erro.getMessage());
        assertEquals("400", erro.getCode());
    }

    /**
     * POST /api/v1/user
     *
     * Criação do usuário com todas as condições satisfeitas
     */
    @Test
    void testUserErro() {
        UsuarioRegister usuarioRegister = new UsuarioRegister(uuid(), uuid(), uuid());
        usuarioRegister.setUsername(null);

        isBadRequest(usuarioRegister);

        usuarioRegister.setUsername("");

        isBadRequest(usuarioRegister);

        usuarioRegister = new UsuarioRegister(uuid(), uuid(), uuid());
        usuarioRegister.setNome(null);
        isBadRequest(usuarioRegister);

        usuarioRegister.setNome("");
        isBadRequest(usuarioRegister);

        usuarioRegister = new UsuarioRegister(uuid(), uuid(), uuid());
        usuarioRegister.setSenha(null);
        isBadRequest(usuarioRegister);

        usuarioRegister.setSenha("");
        isBadRequest(usuarioRegister);
    }

    private void isBadRequest(UsuarioRegister usuarioRegister) {
        this.testClient.post().uri("/api/v1/register")
                .bodyValue(usuarioRegister)
                .exchange().expectStatus().isBadRequest();
    }
}
