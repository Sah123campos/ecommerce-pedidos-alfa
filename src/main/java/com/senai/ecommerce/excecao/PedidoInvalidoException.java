package com.senai.ecommerce.excecao;

/**
 * Lançada quando a operação pedida é incompatível com a situação atual
 * do pedido: pagar um pedido sem itens, pagar um pedido já pago,
 * alterar os itens de um pedido já pago. É checked: quem chamou pode
 * reagir — informar o cliente, redirecionar para o histórico do
 * pedido, não tentar cobrar de novo — em vez de travar o fluxo.
 */
public class PedidoInvalidoException extends ECommerceException {
    private final String numeroPedido;
    private final String motivo;

    public PedidoInvalidoException(String numeroPedido, String motivo) {
        super("Pedido " + numeroPedido + ": " + motivo);
        this.numeroPedido = numeroPedido;
        this.motivo = motivo;
    }

    public PedidoInvalidoException(String numeroPedido, String motivo, Throwable causa) {
        super("Pedido " + numeroPedido + ": " + motivo, causa);
        this.numeroPedido = numeroPedido;
        this.motivo = motivo;
    }

    public String getNumeroPedido() {
        return numeroPedido;
    }

    public String getMotivo() {
        return motivo;
    }
}
