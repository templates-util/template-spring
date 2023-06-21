package br.com.updev.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class UsuarioRegister {

    @NotNull(message = "O nome deve ser enviado")
    @NotEmpty(message = "O nome deve ser enviado")
    @NotBlank(message = "O nome deve ser enviado")
    private String nome;

    @NotNull(message = "O email deve ser enviado")
    @NotEmpty(message = "O email deve ser enviado")
    @NotBlank(message = "O email deve ser enviado")
    private String username;

    @NotNull(message = "a senha deve ser enviado")
    @NotEmpty(message = "a senha deve ser enviado")
    @NotBlank(message = "a senha deve ser enviado")
    private String senha;

    public UsuarioRegister() {
        // Construtor padrao
    }

    public UsuarioRegister(@NotNull(message = "O nome deve ser enviado") String nome, @NotNull(message = "O email deve ser enviado") String username, @NotNull(message = "a senha deve ser enviado") String senha) {
        this.nome = nome;
        this.username = username;
        this.senha = senha;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
