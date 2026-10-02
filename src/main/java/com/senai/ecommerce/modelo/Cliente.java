package com.senai.ecommerce.modelo;

import com.senai.ecommerce.util.Validador;

/**
 * Aula 09: todo cliente nasce validado. Nome, CPF e e-mail inválidos
 * são defeito de quem chamou o cadastro — por isso as validações usam
 * {@link IllegalArgumentException} (via {@link Validador}), unchecked,
 * em vez de uma exceção customizada: não há nada de útil a fazer em
 * tempo de execução além de corrigir o dado enviado.
 */
public class Cliente {
    private String nome;
    private String cpf;
    private String email;
    private String telefone;
    private Endereco endereco;

    // construtor chama os próprios setters para que a validação exista
    // em um único lugar, não duplicada aqui
    public Cliente(String nome, String cpf, String email, Endereco endereco) {
        setNome(nome);
        setCpf(cpf);
        setEmail(email);
        this.endereco = endereco;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        Validador.exigirNaoVazio(nome, "Nome do cliente é obrigatório");
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        // validação simples de formato (11 dígitos); não verifica os dígitos
        // verificadores do CPF — isso fica para quando houver uma regra de
        // negócio real exigindo isso
        Validador.exigirCpfValido(cpf, "CPF inválido: " + cpf);
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        Validador.exigirEmailValido(email, "E-mail inválido: " + email);
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Endereco getEndereco() {
        return endereco;
    }

    public void setEndereco(Endereco endereco) {
        this.endereco = endereco;
    }

    @Override
    public String toString() {
        return "Cliente [nome=" + nome + ", cpf=" + cpf + ", email=" + email + ", telefone=" + telefone + ", endereco="
                + endereco.toString() + "]";
    }
}
