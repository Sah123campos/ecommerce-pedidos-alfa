package com.senai.ecommerce.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.senai.ecommerce.excecao.EstoqueInsuficienteException;
import com.senai.ecommerce.excecao.PagamentoRecusadoException;
import com.senai.ecommerce.excecao.PedidoInvalidoException;
import com.senai.ecommerce.modelo.pagamento.ProcessadorPagamento;
import com.senai.ecommerce.util.Validador;

public class Pedido {
    private final String numero;
    private final Cliente cliente; // associação 1 para 1, obrigatória
    private final List<ItemPedido> itens = new ArrayList<>(); // composição: o pedido é dono dos itens
    private ProcessadorPagamento formaPagamento; // associação 0..1 — só o contrato, nunca uma classe concreta
    private SituacaoDoPedido situacao = SituacaoDoPedido.ABERTO;

    public Pedido(String numero, Cliente cliente) {
        Validador.exigirNaoNulo(cliente, "Pedido exige um cliente");
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

    /**
     * Versão completa: contém toda a lógica e validação.
     *
     * {@code produto == null} e {@code quantidade <= 0} são defeito de
     * quem chamou — unchecked. Estoque insuficiente é situação de
     * negócio prevista — checked, propagada de {@link Produto#baixarEstoque}.
     * Alterar um pedido já pago também é checked: quem chamou pode
     * reagir (abrir um pedido novo, por exemplo).
     */
    public void adicionarItem(Produto produto, int quantidade)
            throws EstoqueInsuficienteException, PedidoInvalidoException {
        Validador.exigirNaoNulo(produto, "Produto é obrigatório");
        if (situacao == SituacaoDoPedido.PAGO) {
            throw new PedidoInvalidoException(numero, "não é possível alterar um pedido já pago");
        }
        produto.baixarEstoque(quantidade); // pode lançar EstoqueInsuficienteException
        itens.add(new ItemPedido(produto, quantidade, produto.getPreco()));
    }

    // sobrecarga (polimorfismo estático): conveniência para o caso mais comum —
    // delega para a versão completa, nunca duplica a regra nem o throws
    public void adicionarItem(Produto produto) throws EstoqueInsuficienteException, PedidoInvalidoException {
        adicionarItem(produto, 1);
    }

    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(itens);
    }

    /**
     * Paga o pedido usando qualquer forma que cumpra o contrato
     * {@link ProcessadorPagamento}. Nenhuma classe concreta é citada
     * aqui — e é por isso que este método não precisará mudar quando
     * surgir uma forma de pagamento nova (Aula 08).
     *
     * Pedido sem itens e pedido já pago são situações de negócio
     * previstas: {@link PedidoInvalidoException}, checked. Pagamento
     * não aprovado pelo processador também é previsto — vira
     * {@link PagamentoRecusadoException}, e a situação do pedido
     * permanece ABERTO, para que o cliente possa tentar outra forma.
     */
    public void pagar(ProcessadorPagamento processador) throws PedidoInvalidoException, PagamentoRecusadoException {
        Validador.exigirNaoNulo(processador, "Forma de pagamento é obrigatória");
        if (itens.isEmpty()) {
            throw new PedidoInvalidoException(numero, "pedido sem itens não pode ser pago");
        }
        if (situacao == SituacaoDoPedido.PAGO) {
            throw new PedidoInvalidoException(numero, "pedido já está pago");
        }

        boolean aprovado = processador.processar(calcularValorTotal());
        if (!aprovado) {
            // situação permanece ABERTO: o cliente pode tentar outra forma de pagamento
            throw new PagamentoRecusadoException(processador.getDescricao(), "pagamento não aprovado no momento");
        }

        this.formaPagamento = processador;
        this.situacao = SituacaoDoPedido.PAGO;
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
