package com.senai.ecommerce.modelo.pagamento;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Regra própria do cartão: recusa mais de 12 parcelas e exige um valor
 * mínimo por parcela. As duas violações são erro de uso da classe —
 * por isso lançam exceção, e não devolvem {@code false}.
 */
public class CartaoCredito extends FormaPagamento implements Estornavel {
    private static final int MAXIMO_DE_PARCELAS = 12;
    private static final BigDecimal VALOR_MINIMO_PARCELA = new BigDecimal("5.00");

    private final String numeroMascarado;
    private final int parcelas;

    public CartaoCredito(BigDecimal valor, String numeroMascarado, int parcelas) {
        super(valor);
        if (numeroMascarado == null || numeroMascarado.isBlank()) {
            throw new IllegalArgumentException("Número do cartão é obrigatório");
        }
        if (parcelas <= 0) {
            throw new IllegalArgumentException("Quantidade de parcelas deve ser positiva");
        }
        if (parcelas > MAXIMO_DE_PARCELAS) {
            throw new IllegalArgumentException(
                    "Cartão não aceita mais de " + MAXIMO_DE_PARCELAS + " parcelas");
        }
        BigDecimal valorPorParcela = valor.divide(BigDecimal.valueOf(parcelas), 2, RoundingMode.HALF_UP);
        if (valorPorParcela.compareTo(VALOR_MINIMO_PARCELA) < 0) {
            throw new IllegalArgumentException(
                    "Valor por parcela abaixo do mínimo de R$ " + VALOR_MINIMO_PARCELA);
        }
        this.numeroMascarado = numeroMascarado;
        this.parcelas = parcelas;
    }

    public int getParcelas() {
        return parcelas;
    }

    @Override
    public boolean processar(BigDecimal valor) {
        System.out.println("Autorizando cartão " + numeroMascarado + " em " + parcelas + "x");
        registrarComprovante("CARTAO-" + System.currentTimeMillis());
        return true; // simulação: limite sempre suficiente
    }

    @Override
    public boolean estornar(BigDecimal valor) {
        System.out.println("Estornando R$ " + valor + " para o cartão " + numeroMascarado);
        return true;
    }

    @Override
    public String getDescricao() {
        return super.getDescricao() + " (Cartão " + numeroMascarado + ", " + parcelas + "x)";
    }

    @Override
    public boolean permiteParcelamento() {
        return true;
    }
}
