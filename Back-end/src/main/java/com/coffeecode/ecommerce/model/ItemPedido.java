package com.coffeecode.ecommerce.model;

public class ItemPedido {
    private Produto produto;
    private int quantidade;
    private double precoPraticado;

    // Construtor que utiliza os métodos set para inicializar os atributos
    public ItemPedido(Produto produto, int quantidade, double precoPraticado) {
        setProduto(produto);
        setQuantidade(quantidade);
        setPrecoPraticado(precoPraticado);
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("O produto não pode ser nulo.");
        }
        this.produto = produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero.");
        }
        this.quantidade = quantidade;
    }

    public double getPrecoPraticado() {
        return precoPraticado;
    }

    public void setPrecoPraticado(double precoPraticado) {
        if (precoPraticado < 0) {
            throw new IllegalArgumentException("O preço praticado não pode ser negativo.");
        }
        this.precoPraticado = precoPraticado;
    }

    public double calcularSubtotal() {
        return this.precoPraticado * this.quantidade; 
    }
}