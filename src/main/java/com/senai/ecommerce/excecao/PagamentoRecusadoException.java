package com.senai.ecommerce.excecao;

/**
 * Lançada quando o processador de pagamento não aprova a transação
 * (ex.: cartão sem limite, boleto ainda não compensado). É checked:
 * quem chamou pode oferecer outra forma de pagamento ao cliente, em
 * vez de simplesmente travar.
 */
public class PagamentoRecusadoException extends ECommerceException {
    private final String formaPagamento;
    private final String motivo;

    public PagamentoRecusadoException(String formaPagamento, String motivo) {
        super("Pagamento por " + formaPagamento + " não aprovado: " + motivo);
        this.formaPagamento = formaPagamento;
        this.motivo = motivo;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public String getMotivo() {
        return motivo;
    }
}
