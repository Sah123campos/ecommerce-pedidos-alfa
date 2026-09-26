package com.senai.ecommerce.modelo.pagamento;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Regra própria do Boleto: o vencimento não pode ser no passado (erro
 * de uso da classe -> exceção), e {@code processar} sempre devolve
 * {@code false}, porque a aprovação depende da compensação bancária —
 * um evento futuro, situação de negócio prevista, nunca exceção.
 */
public class Boleto extends FormaPagamento {
    private final LocalDate vencimento;

    public Boleto(BigDecimal valor, LocalDate vencimento) {
        super(valor);
        if (vencimento == null || vencimento.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Vencimento do boleto não pode ser no passado");
        }
        this.vencimento = vencimento;
    }

    public LocalDate getVencimento() {
        return vencimento;
    }

    @Override
    public boolean processar(BigDecimal valor) {
        System.out.println("Gerando boleto de R$ " + valor + ", vencimento em " + vencimento);
        // a compensação acontece depois; este boleto nunca é aprovado na hora
        return false;
    }

    @Override
    public String getDescricao() {
        return super.getDescricao() + " (Boleto, vence em " + vencimento + ")";
    }
}
