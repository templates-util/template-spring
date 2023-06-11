package br.com.updev.controllers;

import br.com.updev.domain.Perfil;
import br.com.updev.domain.Permissao;
import br.com.updev.dto.*;
import br.com.updev.repositories.PerfilRepository;
import br.com.updev.repositories.PermissaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureWebTestClient

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class PerfilControllerTest {

    @Autowired
    private WebTestClient testClient;

    @Autowired
    private AuthController authController;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private PermissaoRepository permissaoRepository;

    private Autorizacao auth;

    @BeforeEach
    void before() {
        Credenciais credenciais = new Credenciais("admin@itexto.com.br", "admin1234");

        this.auth = authController.autenticar(credenciais).getBody();
    }

    @Test
    void testListagemPerfis() {
        List<Permissao> permissoes = permissaoRepository.findAll();

        List<PermissaoResponse> result = testClient.get().uri("/api/v1/roles")
                .header("Authorization", this.auth.getToken())
                .exchange().expectBodyList(PermissaoResponse.class).returnResult().getResponseBody();

        assertNotNull(result);
        assertEquals(permissoes.size(), result.size(), "Deveria ter o mesmo número de registros");

        for (Permissao perm : permissoes) {
            assertTrue(result.stream().anyMatch(p -> p.getAuthority().equals(perm.getAuthority())), "Não foi retornada a permissão " + perm.getAuthority());
        }

    }

    @Test
    void testObtencaoDoPerfil() {
        Perfil perfil = perfilRepository.findByNome("Administrador");
        assertNotNull(perfil, "Sem perfil para testar");

        PerfilResponse result = testClient.get().uri("/api/v1/profile/" + perfil.getUuid())
                .header("Authorization", this.auth.getToken())
                .exchange().expectBody(PerfilResponse.class).returnResult().getResponseBody();
        assertNotNull(result, "Não nos retornou o resultado");
        assertEquals(perfil.getUuid(), result.getUuid(), "UUID retornado é diferente");
        assertEquals(perfil.getNome(), result.getName(), "Nome retornado é diferente");
        List<PermissaoResponse> roles = result.getRoles();
        for (Permissao permissao : perfil.getPermissoes()) {
            boolean presente =
                    roles.stream().anyMatch(r -> r.getAuthority().equals((permissao.getAuthority())));
            assertTrue(presente, "Permissão " + permissao.getAuthority() + " ausente");

        }

        /*
         * Se o perfil não existe, tem de me retornar um 404
         */
        testClient.get().uri("/api/v1/profile/28282728")
                .header("Authorization", auth.getToken())
                .exchange()
                .expectStatus().isEqualTo(404);
    }

    @Test
    void testUpdate() {
        PerfilEdit dto = new PerfilEdit();
        dto.setName("Teste - " + UUID.randomUUID());
        dto.setActive(true);
        dto.setRoles(new ArrayList<>());
        dto.getRoles().add("ROLE_PROFILE_CREATE");
        dto.getRoles().add("ROLE_PROFILE_UPDATE");

        PerfilResponse result = testClient.post().uri("/api/v1/profile")
                .bodyValue(dto)
                .header("Authorization", auth.getToken())
                .exchange().expectBody(PerfilResponse.class).returnResult().getResponseBody();
        assertNotNull(result);
        Perfil registroCriado = perfilRepository.findByUuid(result.getUuid());
        assertNotNull(registroCriado, "O registro não foi criado");

        dto.getRoles().clear();
        dto.getRoles().add("ROLE_ADMIN");

        testClient.put().uri("/api/v1/profile/" + registroCriado.getUuid())
                .bodyValue(dto)
                .header("Authorization", auth.getToken())
                .exchange().expectStatus().isEqualTo(200);
        Perfil registroEditado = perfilRepository.findByUuid(registroCriado.getUuid());
        assertNotNull(registroEditado);
        assertNotNull(registroEditado.getPermissoes());
        assertEquals(1, registroEditado.getPermissoes().size());
        boolean presente = registroEditado.getPermissoes().stream().anyMatch(p -> p.getAuthority().equals("ROLE_ADMIN"));
        assertTrue(presente, "Permissão ROLE_ADMIN ausente");

        /**
         * Se eu tentar editar um perfil inexistente, tem de retornar 404
         */
        
        this.testClient.put().uri("/api/v1/profile/38237237")
                .bodyValue(dto)
                .header("Authorization", auth.getToken())
                .exchange().expectStatus().isEqualTo(404);

    }

    /**
     * Se tento cadastrar um perfil com mesmo nome duas vezes, um erro deve
     * ser retornado
     */
    @Test
    void testCreateMesmoNome() {
        PerfilEdit dto = new PerfilEdit();
        dto.setName("Teste - " + UUID.randomUUID());
        dto.setActive(true);
        dto.setRoles(new ArrayList<>());
        dto.getRoles().add("ROLE_PROFILE_CREATE");
        dto.getRoles().add("ROLE_PROFILE_UPDATE");

        testClient.post().uri("/api/v1/profile")
                .bodyValue(dto)
                .header("Authorization", auth.getToken())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CREATED)
                .expectBody(PerfilResponse.class);

        /**
         * Segunda vez que cadastro com mesmo perfil e mesmo nome
         */
        testClient.post().uri("/api/v1/profile")
                .bodyValue(dto)
                .header("Authorization", auth.getToken())
                .exchange().expectStatus().isBadRequest();
    }

    /**
     * Se tento criar um perfil sem nome, deve me retornar um erro do tipo 400
     */
    @Test
    void testCreateSemNome() {
        PerfilEdit dto = new PerfilEdit();
        dto.setActive(true);
        dto.setRoles(new ArrayList<>());
        dto.getRoles().add("ROLE_PROFILE_CREATE");

        testClient.post().uri("/api/v1/profile")
                .bodyValue(dto)
                .header("Authorization", this.auth.getToken())
                .exchange()
                .expectStatus().isEqualTo(400);
    }

    /*
     * Se a role é indefinida, tem de me retornar um erro do tipo 400
     */
    @Test
    void testCreateRoleIndefinida() {
        PerfilEdit dto = new PerfilEdit();
        dto.setName("Teste - " + UUID.randomUUID());
        dto.setActive(true);
        dto.setRoles(new ArrayList<>());
        dto.getRoles().add("ROLE_#$93843k348");

        testClient.post().uri("/api/v1/profile")
                .bodyValue(dto)
                .header("Authorization", this.auth.getToken())
                .exchange()
                .expectStatus().isEqualTo(400);
    }

    @Test
    void testCreate() {
        PerfilEdit dto = new PerfilEdit();
        dto.setName("Teste - " + UUID.randomUUID());
        dto.setActive(true);
        dto.setRoles(new ArrayList<>());
        dto.getRoles().add("ROLE_PROFILE_CREATE");
        dto.getRoles().add("ROLE_PROFILE_UPDATE");

        PerfilResponse result = testClient.post().uri("/api/v1/profile")
                .bodyValue(dto)
                .header("Authorization", auth.getToken())
                .exchange().expectBody(PerfilResponse.class).returnResult().getResponseBody();
        assertNotNull(result);
        assertNotNull(result.getUuid(), "Não retornou o UUID do perfil");

        Perfil perfil = perfilRepository.findByUuid(result.getUuid());
        assertNotNull(perfil, "Registro do perfil não foi encontrado");

        assertTrue(perfil.getPermissoes().stream().anyMatch(p -> p.getAuthority().equals("ROLE_PROFILE_CREATE")), "Permissão ROLE_PROFILE_UPDATE não foi incluída");

    }

    @Test
    void testList() {
        List<PerfilResponse> resposta = testClient.get().uri("/api/v1/profile")
                .header("Authorization", auth.getToken())
                .exchange().expectBodyList(PerfilResponse.class).returnResult().getResponseBody();

        assertNotNull(resposta, "Retornou uma resposta nula");
        assertFalse(resposta.isEmpty(), "Retornou uma lista vazia. Todos os perfis deveriam ser retornados aqui");

        List<Perfil> registros = perfilRepository.findAll();
        assertFalse(registros.isEmpty(), "Não existem perfis cadastrados no sistema, Como posso ter retornado perfis acima?");

        for (Perfil perfil : registros) {
            boolean presente = false;
            for (PerfilResponse pr : resposta) {
                if (pr.getUuid().equals(perfil.getUuid())) {
                    presente = true;
                    break;
                }
            }

            assertTrue(presente, "Não foi encontrado o perfil " + perfil.getNome());
        }

        List<PerfilResponse> respostaVazia = testClient.get().uri(
                UriBuilder -> UriBuilder.path("/api/v1/profile").queryParam("name", UUID.randomUUID().toString()).build())
                .header("Authorization", auth.getToken())
                .exchange().expectBodyList(PerfilResponse.class).returnResult().getResponseBody();
        assertNotNull(respostaVazia);
        assertTrue(respostaVazia.isEmpty(), "Deveria retornar uma lista vazia");


        /*
         * Se fornecer uma credencial inválida (token), tem de me retornar um erro do tipo 401
         */
        testClient.get().uri("/api/v1/profile").header("Authorization", "asdfasdf").exchange().expectStatus().is4xxClientError();
        /*
         * Se não fornecer credencial nenhuma, tambémtem de me retornar um erro 401
         */
        testClient.get().uri("/api/v1/profile").exchange().expectStatus().is4xxClientError();


    }

}
