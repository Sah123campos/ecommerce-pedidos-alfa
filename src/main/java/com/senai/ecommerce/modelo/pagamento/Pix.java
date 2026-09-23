package com.senai.ecommerce.modelo.pagamento;

import java.math.BigDecimal;

public class Pix extends FormaPagamento {
    private String chave;

    public Pix(BigDecimal valor, String chave) {
        super(valor);
        setChave(chave);
    }

    public void setChave(String chave) {
        if (chave == null || chave.isBlank()) {
            throw new IllegalArgumentException("Chave Pix é obrigatória");
        }
        this.chave = chave;
    }

    @Override
    public boolean processar() {
        // simulação: um Pix é aprovado na hora
        System.out.println("Processando Pix para a chave " + chave);
        return true;
    }

    @Override
    public String getResumo() {
        return super.getResumo() + " (chave " + chave + ")";
    }
}
