package com.senai.ecommerce.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.senai.ecommerce.modelo.pagamento.ProcessadorPagamento;

public class Pedido {
    private final String numero;
    private final Cliente cliente; 
    private final List<ItemPedido> itens = new ArrayList<>(); 
    private ProcessadorPagamento formaPagamento; 
    private SituacaoDoPedido situacao = SituacaoDoPedido.ABERTO;

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

    public SituacaoDoPedido getSituacao() {
        return situacao;
    }

    // versão completa: contém toda a lógica e validação
    public void adicionarItem(Produto produto, int quantidade) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto é obrigatório");
        }
        if (!produto.temEstoqueDisponivel(quantidade)) {
            throw new IllegalStateException("Estoque insuficiente: " + produto.getNome());
        }
        itens.add(new ItemPedido(produto, quantidade, produto.getPreco()));
    }

 
    public void adicionarItem(Produto produto) {
        adicionarItem(produto, 1);
    }

    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(itens);
    }

    
    public boolean pagar(ProcessadorPagamento processador) {
        if (processador == null) {
            throw new IllegalArgumentException("Forma de pagamento é obrigatória");
        }
        if (itens.isEmpty()) {
            throw new IllegalStateException("Pedido sem itens não pode ser pago");
        }
        if (situacao == SituacaoDoPedido.PAGO) {
            // decisão da equipe: pedido já pago recusa uma nova tentativa de pagamento
            throw new IllegalStateException("Pedido já está pago");
        }

        boolean aprovado = processador.processar(calcularValorTotal());
        if (aprovado) {
            this.formaPagamento = processador;
            this.situacao = SituacaoDoPedido.PAGO;
        }
        return aprovado;
    }

    public ProcessadorPagamento getFormaPagamento() {
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
        String pagamento = (formaPagamento == null) ? "pendente" : formaPagamento.getDescricao();
        return "Pedido [numero=" + numero + ", cliente=" + cliente.getNome()
                + ", itens=" + itens.size() + ", total=R$ " + calcularValorTotal()
                + ", situacao=" + situacao + ", formaPagamento=" + pagamento + "]";
    }
}
