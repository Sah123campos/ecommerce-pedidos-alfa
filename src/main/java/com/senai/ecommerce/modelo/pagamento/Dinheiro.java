package com.senai.ecommerce.modelo.pagamento;

import java.math.BigDecimal;

/**
 * Quarta forma de pagamento — a prova do princípio aberto/fechado.
 * Nenhum arquivo existente precisou ser alterado para acrescentá-la:
 * ela só precisou cumprir o contrato {@link ProcessadorPagamento}.
 * Repare que ela nem estende {@code FormaPagamento} — a interface não
 * impõe uma origem comum, só uma capacidade.
 *
 * Regra própria: recusa valor recebido menor que o total (situação de
 * negócio prevista -> {@code false}, sem exceção) e calcula o troco
 * quando o pagamento é aprovado.
 */
public class Dinheiro implements ProcessadorPagamento {
    private final BigDecimal valorRecebido;
    private BigDecimal troco = BigDecimal.ZERO;
    private String comprovante;

    public Dinheiro(BigDecimal valorRecebido) {
        if (valorRecebido == null || valorRecebido.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor recebido deve ser positivo");
        }
        this.valorRecebido = valorRecebido;
    }

    public BigDecimal getTroco() {
        return troco;
    }

    @Override
    public boolean processar(BigDecimal valor) {
        if (valorRecebido.compareTo(valor) < 0) {
            System.out.println("Valor recebido insuficiente para pagamento em dinheiro");
            return false;
        }
        troco = valorRecebido.subtract(valor);
        comprovante = "DINHEIRO-" + System.currentTimeMillis();
        return true;
    }

    @Override
    public String getComprovante() {
        return comprovante;
    }

    @Override
    public String getDescricao() {
        return "Dinheiro (recebido R$ " + valorRecebido + ")";
    }
}
