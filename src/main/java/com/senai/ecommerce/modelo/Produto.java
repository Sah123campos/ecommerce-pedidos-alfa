package com.senai.ecommerce.modelo;

import com.senai.ecommerce.excecao.EstoqueInsuficienteException;
import com.senai.ecommerce.util.Validador;

// Modelo da classe Produto
public class Produto {
    private String codigo;
    private String nome;
    private String descricao;
    private double preco;
    private int quantidadeEmEstoque;
    private boolean ativo;

    public Produto(String codigo, String nome, double preco, int estoque) {
        // o código identifica o produto no catálogo e não muda depois de criado,
        // por isso é atribuído direto aqui (com validação) em vez de por um setter público
        Validador.exigirNaoVazio(codigo, "Código é obrigatório");
        this.codigo = codigo;
        setNome(nome);
        setPreco(preco);
        setQuantidadeEmEstoque(estoque);
        this.ativo = true;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        Validador.exigirNaoVazio(nome, "Nome do produto é obrigatório");
        this.nome = nome;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        Validador.exigirPositivo(preco, "Preço deve ser maior que zero: " + preco);
        this.preco = preco;
    }

    public int getQuantidadeEmEstoque() {
        return quantidadeEmEstoque;
    }

    public void setQuantidadeEmEstoque(int quantidade) {
        Validador.exigirNaoNegativo(quantidade, "Estoque não pode ser negativo: " + quantidade);
        this.quantidadeEmEstoque = quantidade;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean temEstoqueDisponivel(int quantidadeDesejada) {
        return ativo && this.quantidadeEmEstoque >= quantidadeDesejada;
    }

    /**
     * Dá baixa no estoque. Quantidade inválida (zero ou negativa) é
     * defeito de quem chamou — unchecked, corrige-se no código. Estoque
     * insuficiente é uma situação de negócio perfeitamente possível —
     * checked, porque quem chamou pode e deve reagir a ela (oferecer
     * o que há disponível, sugerir outro produto).
     */
    public void baixarEstoque(int quantidade) throws EstoqueInsuficienteException {
        Validador.exigirPositivo(quantidade, "Quantidade deve ser positiva");
        if (quantidade > quantidadeEmEstoque) {
            throw new EstoqueInsuficienteException(this, quantidade);
        }
        this.quantidadeEmEstoque = this.quantidadeEmEstoque - quantidade;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - R$ %.2f (%d em estoque)",
                codigo, nome, preco, quantidadeEmEstoque);
    }
}
