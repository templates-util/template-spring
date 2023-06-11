package br.com.updev.controllers;

import br.com.updev.domain.Permissao;
import br.com.updev.domain.Usuario;
import br.com.updev.dto.Autorizacao;
import br.com.updev.dto.Credenciais;
import br.com.updev.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class AuthControllerTest {

    @Autowired
    private AuthController controller;

    @Autowired
    private WebTestClient testClient;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void testAutenticacaoBemSucedida() {

        Credenciais credenciais = new Credenciais("admin@itexto.com.br", "admin1234");
        ResponseEntity<Autorizacao> result = controller.autenticar(credenciais);


        assertEquals(HttpStatus.OK, result.getStatusCode());
        Autorizacao autorizacao = result.getBody();
        assertNotNull(autorizacao, "Não retornou as autorizações");

        assertEquals(credenciais.getUsername(), autorizacao.getUsername(), "Retornou o username incorreto");
        assertNotNull(autorizacao.getToken(), "Não nos retornou um token");
        assertNotNull(autorizacao.getName(), "Não retornou o nome do usuário");

        Usuario usuario = usuarioRepository.findByEmail(autorizacao.getUsername());
        assertNotNull(usuario, "O registro do usuário não existe no banco de dados");
        for (Permissao permissao : usuario.getPerfil().getPermissoes()) {
            boolean presente = autorizacao.getRoles().stream().anyMatch(p -> p.equals(permissao.getAuthority()));
            assertTrue(presente, "A permissão " + permissao.getAuthority() + " não foi encontrada");
        }


        /*
         * Se a autenticação está correta, consigo acessar um endpoint de teste
         */
        this.testClient.get().uri("/api/v1/test")
                .header("Authorization", autorizacao.getToken())
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
        ResponseEntity<Autorizacao> result = controller.autenticar(credenciais);
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
        assertNull(result.getBody(), "A resposta deveria vir vazia");


    }

}
