package br.com.updev.controllers;

import br.com.updev.BaseTest;
import br.com.updev.domain.Perfil;
import br.com.updev.domain.Usuario;
import br.com.updev.dto.Autorizacao;
import br.com.updev.dto.Erro;
import br.com.updev.dto.UsuarioDTO;
import br.com.updev.dto.UsuarioRequest;
import br.com.updev.repositories.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UsuarioControllerTest extends BaseTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * POST /api/v1/user
     * Se o usuário não tiver permissão de acesso ao endpoint o código
     * 401 deverá ser retornado
     */
    @Test
    void testCriarUsuarioAcessoNegadoRetornar403() {
        UsuarioRequest dto = createUsuarioRequest();

        // token invalido
        this.httpClient.post().uri("/api/v1/user")
                .header("Authorization", uuid())
                .bodyValue(dto)
                .exchange()
                .expectStatus().isUnauthorized();

        // sem token
        this.httpClient.post().uri("/api/v1/user")
                .bodyValue(dto)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    /**
     * POST /api/v1/user
     * Não podem existir dois usuários com mesmo login cadastrado, o email deve ser unico.
     * Deve ser retornado código 400 informando a causa do problema
     */
    @Test
    void testCreateUsuarioEmailDuplicado() {
        UsuarioRequest request = createUsuarioRequest();


        this.httpClient.post().uri("/api/v1/user")
                .header("Authorization", this.authAdmin().getToken())
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        // agora vamos tentar novamente criar outro usuario com o mesmo request
        Erro erro = this.httpClient.post().uri("/api/v1/user")
                .header("Authorization", this.authAdmin().getToken())
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(Erro.class).returnResult().getResponseBody();
        assertNotNull(erro);
        assertEquals("400", erro.getCode());
        assertEquals("Já existe um usuário cadastrado com este e-mail", erro.getMessage());

    }

    /**
     * POST /api/v1/user
     *
     * Criação do usuário com todas as condições satisfeitas
     */
    @Test
    void testCriarUsuario() {
        UsuarioRequest request = createUsuarioRequest();

        UsuarioDTO result = this.httpClient.post().uri("/api/v1/user")
                .header("Authorization", this.authAdmin().getToken())
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(201)
                .expectBody(UsuarioDTO.class).returnResult().getResponseBody();

        validarCriacaoEdicaoBemSucessidida(request, result);
    }

    private void validarCriacaoEdicaoBemSucessidida(UsuarioRequest request, UsuarioDTO result) {
        assertNotNull(result);
        assertEquals(request.getUsername(), result.getUsername());
        assertEquals(request.getNome(), result.getNome());
        assertEquals(request.getProfile(), result.getProfile());
        assertNotNull(result.getDateCreated());
        assertNotNull(result.getUuid());
        assertTrue(result.isAtivo());

        Autorizacao authCriado = this.autenticar(request.getUsername(), request.getPassword());
        assertNotNull(authCriado);
    }

    /**
     * POST /api/v1/user
     *
     * Caso seja enviado perfil invalido deve retornar erro para o usuario
     */
    @Test
    void testCreateUsuarioPerfilInvalido() {
        UsuarioRequest request = createUsuarioRequest();
        request.setProfile(uuid());

        Erro erro = this.httpClient.post().uri("/api/v1/user")
                .header("Authorization", this.authAdmin().getToken())
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(Erro.class).returnResult().getResponseBody();
        assertNotNull(erro);
        assertEquals("400", erro.getCode());
        assertEquals("Perfil desconhecido", erro.getMessage());
    }

    /**
     * POST /api/v1/user
     *
     * Caso nao seja enviado todos os parametros para a criação do usuario
     * deve retornar erro 400
     */
    @Test
    void testCreateUsuarioFaltandoParametroObrigatorioRetornarErro() {
        UsuarioRequest request = createUsuarioRequest();

        // Sem usernama
        request.setUsername(null);
        badRequestCreated(request);
        request.setUsername("");
        badRequestCreated(request);

        // sem senha
        request = createUsuarioRequest();
        request.setPassword(null);
        badRequestCreated(request);
        request.setPassword("");
        badRequestCreated(request);

        // Sem nome
        request = createUsuarioRequest();
        request.setNome(null);
        badRequestCreated(request);
        request.setNome("");
        badRequestCreated(request);

        // Sem profile
        request = createUsuarioRequest();
        request.setProfile(null);
        badRequestCreated(request);
        request.setProfile("");
        badRequestCreated(request);
    }

    private void badRequestCreated(UsuarioRequest request) {
        this.httpClient.post().uri("/api/v1/user")
                .header("Authorization", this.authAdmin().getToken())
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(Erro.class).returnResult().getResponseBody();
    }

    /**
     * PUT /api/v1/user/{uuid}
     *
     * Se o usuário não tiver permissão de acesso ao endpoint então o código 403
     * deverá ser retornado
     */
    @Test
    void testUpdateUsuarioAcessoNegadoRetornar403() {
        UsuarioDTO original = this.criarUsuario(getPerfilAdmin(), uuid() + "@teste.com", uuid());

        UsuarioRequest dto = createUsuarioRequest();

        // token invalido
        this.httpClient.put().uri("/api/v1/user/" + original.getUuid())
                .header("Authorization", uuid())
                .bodyValue(dto)
                .exchange()
                .expectStatus().isUnauthorized();

        // sem token
        this.httpClient.put().uri("/api/v1/user/" + original.getUuid())
                .bodyValue(dto)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    /**
     * PUT /api/v1/user/{uuid}
     *
     * Se o identificador do usuário for inválido (inexistente), o código 404 deve
     * ser retornado
     */
    @Test
    void testUpdateUsuarioInexistente() {
        UsuarioRequest dto = createUsuarioRequest();

        Erro erro = this.httpClient.put().uri("/api/v1/user/" + uuid())
                .header("Authorization", authAdmin().getToken())
                .bodyValue(dto)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(Erro.class).returnResult().getResponseBody();
        assertNotNull(erro);
        assertEquals("NOT_FOUND", erro.getCode());
        assertEquals("Usuário não encontrado.", erro.getMessage());
    }

    /**
     * PUT /api/v1/user/{uuid}
     *
     * Editando um usuário com todas as condições satisfeitas
     */
    @Test
    void testUpdateUsuarioOk() {
        UsuarioDTO original = this.criarUsuario(getPerfilAdmin(), uuid() + "@teste.com", uuid());

        UsuarioRequest dto = createUsuarioRequest();

        UsuarioDTO result = this.httpClient.put().uri("/api/v1/user/" + original.getUuid())
                .header("Authorization", this.authAdmin().getToken())
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UsuarioDTO.class).returnResult().getResponseBody();

        validarCriacaoEdicaoBemSucessidida(dto, result);
    }

    /**
     * PUT /api/v1/user/{uuid}
     *
     * Se na hora da edição for informado um email ja em uso pertencente
     * a outro usuario retornar erro
     */
    @Test
    void testUpdateUsuarioAlterandoParaEmailUsadoPorOutroUsuarioRetornarErro400() {
        String email = uuid() + "@teste.com";
        this.criarUsuario(getPerfilAdmin(), email, uuid());
        UsuarioDTO usuarioASerEditado = this.criarUsuario(getPerfilAdmin(), uuid(), uuid());

        UsuarioRequest dto = createUsuarioRequest();
        dto.setUsername(email);
        Erro erro = this.httpClient.put().uri("/api/v1/user/" + usuarioASerEditado.getUuid())
                .header("Authorization", authAdmin().getToken())
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(Erro.class).returnResult().getResponseBody();
        assertNotNull(erro);
        assertEquals("400", erro.getCode());
        assertEquals("Já existe outro usuário cadastrado com este e-mail", erro.getMessage());

    }

    /**
     * PUT /api/v1/user/{uuid}
     *
     * Caso nao seja enviado todos os parametros para a criação do usuario
     * deve retornar erro 400
     */
    @Test
    void testEdicaoUsuarioFaltandoParametroObrigatorioRetornarErro() {
        UsuarioRequest request = createUsuarioRequest();

        // Sem usernama
        request.setUsername(null);
        badRequestEdited(request);
        request.setUsername("");
        badRequestEdited(request);

        // Sem nome
        request = createUsuarioRequest();
        request.setNome(null);
        badRequestEdited(request);
        request.setNome("");
        badRequestEdited(request);

        // Sem profile
        request = createUsuarioRequest();
        request.setProfile(null);
        badRequestEdited(request);
        request.setProfile("");
        badRequestEdited(request);
    }

    private void badRequestEdited(UsuarioRequest request) {
        UsuarioDTO original = this.criarUsuario(getPerfilAdmin(), uuid() + "@teste.com", uuid());

        this.httpClient.put().uri("/api/v1/user/" + original.getUuid())
                .header("Authorization", this.authAdmin().getToken())
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    /**
     * GET /api/v1/user
     *
     * Busca de usuários com todas as condições satisfeitas sem passar nenhum parametro
     * deve retornar o max de 50 e ordenador pelo nome
     */
    @Test
    void testBuscaPadrao() {
        Autorizacao authAdmin = this.authAdmin();
        gerarVariosUsuarios(Objects.requireNonNull(rand()).nextInt(10) + 50);

        List<UsuarioDTO> result = this.httpClient.get().uri("/api/v1/user")
                .header("Authorization", authAdmin.getToken())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UsuarioDTO.class)
                .returnResult().getResponseBody();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(50, result.size());

        verificarOrdenacao(result);
    }


    /**
     * GET /api/v1/user
     *
     * Se o usuário não tiver permissão de acesso ao endpoint então o código 403
     * deverá ser retornado
     */
    @Test
    void testGETUsuarioAcessoNegadoRetornar403() {
        // token invalido
        this.httpClient.get().uri("/api/v1/user")
                .header("Authorization", uuid())
                .exchange()
                .expectStatus().isUnauthorized();

        // sem token
        this.httpClient.get().uri("/api/v1/user")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    /**
     * GET /api/v1/user
     *
     * Busca de usuários com todas as condições satisfeitas, parametro de paginacao
     * deve retornar paginado
     */
    @Test
    void testBuscaPadraoParametroPaginacao() {
        Autorizacao authAdmin = this.authAdmin();
        gerarVariosUsuarios(Objects.requireNonNull(rand()).nextInt(10) + 5);

        List<UsuarioDTO> result = this.httpClient.get().uri("/api/v1/user?pageSize=5")
                .header("Authorization", authAdmin.getToken())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UsuarioDTO.class)
                .returnResult().getResponseBody();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(5, result.size());

        verificarOrdenacao(result);
    }

    /**
     * GET /api/v1/user
     *
     * Busca de usuários com todas as condições satisfeitas
     * parametro nome
     */
    @Test
    void testBuscaPadraoParametroNome() {
        Autorizacao authAdmin = this.authAdmin();
        UsuarioDTO original = this.criarUsuario(getPerfilAdmin(), uuid() + "@teste.com", uuid());

        List<UsuarioDTO> result = this.httpClient.get().uri("/api/v1/user?nome=" + original.getNome())
                .header("Authorization", authAdmin.getToken())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UsuarioDTO.class)
                .returnResult().getResponseBody();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch((user) -> user.getNome().contains(original.getNome())));

        verificarOrdenacao(result);
    }

    /**
     * GET /api/v1/user
     *
     * Busca de usuários com todas as condições satisfeitas
     * parametro email
     */
    @Test
    void testBuscaPadraoParametroEmail() {
        Autorizacao authAdmin = this.authAdmin();
        UsuarioDTO original = this.criarUsuario(getPerfilAdmin(), uuid() + "@teste.com", uuid());

        List<UsuarioDTO> result = this.httpClient.get().uri("/api/v1/user?email=" + original.getUsername())
                .header("Authorization", authAdmin.getToken())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UsuarioDTO.class)
                .returnResult().getResponseBody();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch((user) -> user.getUsername().contains(original.getUsername())));

        verificarOrdenacao(result);
    }


    private void gerarVariosUsuarios(int numeroUsuariosCriar) {
        List<Usuario> usuarios = new ArrayList<>();
        Perfil perfil = criarPerfil(new ArrayList<>());
        for (int i = 0;i < numeroUsuariosCriar;i++) {
            Usuario usuario = new Usuario();
            usuario.setAtivo(true);
            usuario.setEmail(uuid());
            usuario.setHashSenha(uuid());
            usuario.setNome(uuid());
            usuario.setPerfil(perfil);
            usuarios.add(usuario);
        }
        usuarioRepository.saveAll(usuarios);
    }

    private void verificarOrdenacao(List<UsuarioDTO> result) {
        for (int i = 0;i < result.size() - 1;i++) {
            assertTrue(result.get(i).getNome().compareToIgnoreCase(result.get(i + 1).getNome()) <= 0);
        }
    }
}