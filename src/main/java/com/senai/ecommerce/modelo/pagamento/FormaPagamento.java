package com.senai.ecommerce.modelo.pagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Classe-mãe das formas de pagamento concretas (Aula 06).
 *
 * Guarda o que é comum a todas — valor, momento do pagamento e
 * comprovante — e cumpre o contrato {@link ProcessadorPagamento}
 * (Aula 08). Quem decide COMO processar continua sendo cada
 * subclasse: por isso {@code processar(BigDecimal)} permanece sem
 * implementação aqui.
 */
public abstract class FormaPagamento implements ProcessadorPagamento {
    private BigDecimal valor;
    private LocalDateTime dataDoPagamento;
    private String comprovante;

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

    public LocalDateTime getDataDoPagamento() {
        return dataDoPagamento;
    }

    // as subclasses chamam isto quando o pagamento é efetivamente aprovado
    protected void registrarComprovante(String comprovante) {
        this.comprovante = comprovante;
        this.dataDoPagamento = LocalDateTime.now();
    }

    @Override
    public String getComprovante() {
        return comprovante;
    }

    // comportamento comum, herdado pronto por todas; cada forma pode enriquecer com @Override
    @Override
    public String getDescricao() {
        return String.format("%s no valor de R$ %s", getClass().getSimpleName(), valor);
    }
}
