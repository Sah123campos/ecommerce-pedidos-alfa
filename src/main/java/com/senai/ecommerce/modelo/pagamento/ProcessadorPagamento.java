package com.senai.ecommerce.modelo.pagamento;

import java.math.BigDecimal;

/**
 * Contrato comum a qualquer forma de pagamento aceita pelo sistema.
 *
 * O {@code Pedido} conhece apenas este contrato — nunca uma classe
 * concreta como Pix, Boleto ou CartaoCredito. É isso que permite
 * acrescentar uma forma de pagamento nova sem alterar nenhuma linha
 * de código já existente (princípio aberto/fechado).
 */
public interface ProcessadorPagamento {

    /**
     * Processa o pagamento do valor informado.
     *
     * @return {@code true} se aprovado imediatamente; {@code false} se a
     *         aprovação depende de um evento futuro (ex.: compensação de
     *         um boleto) — nunca lança exceção para uma situação de
     *         negócio prevista.
     */
    boolean processar(BigDecimal valor);

    /** Identificador do pagamento aprovado, gerado por cada forma à sua maneira. */
    String getComprovante();

    /** Descrição amigável do pagamento, para exibição ao cliente. */
    String getDescricao();

    /**
     * Indica se a forma aceita parcelamento.
     * Método default: a maioria das formas não parcela, então só quem
     * precisa (o cartão de crédito) sobrescreve.
     */
    default boolean permiteParcelamento() {
        return false;
    }
}
