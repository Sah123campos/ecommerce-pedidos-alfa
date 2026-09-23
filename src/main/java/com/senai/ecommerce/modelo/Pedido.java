package com.senai.ecommerce.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.senai.ecommerce.modelo.pagamento.FormaPagamento;

public class Pedido {
    private final String numero;
    private final Cliente cliente; // associação 1 para 1, obrigatória
    private final List<ItemPedido> itens = new ArrayList<>(); // composição: o pedido é dono dos itens
    private FormaPagamento formaPagamento; // associação 0..1, opcional

    public Pedido(String numero, Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("Pedido exige um cliente");
        }
        this.numero = numero;
        this.cliente = cliente;
    }

    public String getNumero() {
        return numero;
    }

    public Cliente getCliente() {
        return cliente;
    }

    // composição de verdade: o pedido cria o próprio item, quem chama não entrega um ItemPedido pronto
    public void adicionarItem(Produto produto, int quantidade) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto é obrigatório");
        }
        if (!produto.temEstoqueDisponivel(quantidade)) {
            throw new IllegalStateException("Estoque insuficiente: " + produto.getNome());
        }
        itens.add(new ItemPedido(produto, quantidade, produto.getPreco()));
    }

    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public void pagarCom(FormaPagamento formaPagamento) {
        if (itens.isEmpty()) {
            throw new IllegalStateException("Pedido sem itens não pode ser pago");
        }
        this.formaPagamento = formaPagamento;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public BigDecimal calcularValorTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedido item : itens) {
            total = total.add(BigDecimal.valueOf(item.calcularSubtotal()));
        }
        return total;
    }

    @Override
    public String toString() {
        String pagamento = (formaPagamento == null) ? "pendente" : formaPagamento.getResumo();
        return "Pedido [numero=" + numero + ", cliente=" + cliente.getNome()
                + ", itens=" + itens.size() + ", total=R$ " + calcularValorTotal()
                + ", formaPagamento=" + pagamento + "]";
    }
}
