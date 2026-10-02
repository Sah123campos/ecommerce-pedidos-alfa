package com.senai.ecommerce.excecao;

import com.senai.ecommerce.modelo.Produto;

/**
 * Lançada quando se pede mais unidades de um produto do que há em
 * estoque. É checked: quem chamou pode reagir de forma útil — oferecer
 * a quantidade disponível, sugerir outro produto, avisar o setor de
 * compras. O {@link #getProduto()} é o que torna isso possível; uma
 * {@code String} de mensagem não bastaria.
 */
public class EstoqueInsuficienteException extends ECommerceException {
    private final Produto produto;
    private final int quantidadeSolicitada;

    public EstoqueInsuficienteException(Produto produto, int quantidadeSolicitada) {
        super("Estoque insuficiente de " + produto.getNome()
                + ": disponível " + produto.getQuantidadeEmEstoque()
                + ", solicitado " + quantidadeSolicitada);
        this.produto = produto;
        this.quantidadeSolicitada = quantidadeSolicitada;
    }

    public Produto getProduto() {
        return produto;
    }

    public int getQuantidadeSolicitada() {
        return quantidadeSolicitada;
    }
}
