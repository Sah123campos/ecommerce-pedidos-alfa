package com.senai.ecommerce.modelo.pagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public abstract class FormaPagamento {
    private BigDecimal valor;
    private LocalDateTime dataDoPagamento;

    protected FormaPagamento(BigDecimal valor) {
        setValor(valor);
    }

    public void setValor(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do pagamento deve ser positivo");
        }
        this.valor = valor;
    }

    public BigDecimal getValor() {
        return valor;
    }

    // toda forma de pagamento processa — cada uma do seu jeito
    public abstract boolean processar();

    // comportamento comum, herdado pronto por todas
    public String getResumo() {
        return String.format("%s no valor de R$ %s", getClass().getSimpleName(), valor);
    }
}
