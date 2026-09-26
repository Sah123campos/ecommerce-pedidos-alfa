package com.senai.ecommerce.modelo.pagamento;

import java.math.BigDecimal;

/**
 * Regra própria do Pix: aprovação imediata, desde que a chave seja
 * obrigatória e não vazia.
 */
public class Pix extends FormaPagamento {
    private final String chave;

    public Pix(BigDecimal valor, String chave) {
        super(valor);
        if (chave == null || chave.isBlank()) {
            throw new IllegalArgumentException("Chave Pix é obrigatória");
        }
        this.chave = chave;
    }

    public String getChave() {
        return chave;
    }

    @Override
    public boolean processar(BigDecimal valor) {
        // simulação: um Pix é aprovado na hora
        System.out.println("Enviando cobrança Pix para a chave " + chave);
        registrarComprovante("PIX-" + System.currentTimeMillis());
        return true;
    }

    @Override
    public String getDescricao() {
        return super.getDescricao() + " (Pix, chave " + chave + ")";
    }
}
