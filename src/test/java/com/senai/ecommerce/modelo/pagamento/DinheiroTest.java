package com.senai.ecommerce.modelo.pagamento;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Dinheiro")
class DinheiroTest {

    private Dinheiro dinheiro;

    @BeforeEach
    void prepararCenario() {
        dinheiro = new Dinheiro(new BigDecimal("200.00"));
    }

    private static void assertValor(String esperado, BigDecimal obtido) {
        assertEquals(0, new BigDecimal(esperado).compareTo(obtido), "Esperado " + esperado + " mas foi " + obtido);
    }

    @Test
    @DisplayName("Deve aprovar, calcular o troco e gerar comprovante quando o valor recebido cobre a conta")
    void deveAprovarECalcularTroco() {
        boolean aprovado = dinheiro.processar(new BigDecimal("150.00"));

        assertAll(
                () -> assertTrue(aprovado),
                () -> assertValor("50.00", dinheiro.getTroco()),
                () -> assertTrue(dinheiro.getComprovante().startsWith("DINHEIRO-")));
    }

    @Test
    @DisplayName("Deve aprovar sem troco quando o valor recebido é exatamente o da conta")
    void deveAprovarSemTrocoQuandoValorExato() {
        boolean aprovado = dinheiro.processar(new BigDecimal("200.00"));

        assertAll(
                () -> assertTrue(aprovado),
                () -> assertValor("0", dinheiro.getTroco()));
    }

    @Test
    @DisplayName("Deve recusar sem comprovante nem troco quando o valor recebido é insuficiente")
    void deveRecusarQuandoValorInsuficiente() {
        boolean aprovado = dinheiro.processar(new BigDecimal("200.01"));

        assertAll(
                () -> assertFalse(aprovado),
                () -> assertNull(dinheiro.getComprovante()),
                () -> assertValor("0", dinheiro.getTroco()));
    }

    @Test
    @DisplayName("Deve informar o valor recebido na descrição e não permitir parcelamento")
    void deveInformarValorRecebidoNaDescricao() {
        assertAll(
                () -> assertTrue(dinheiro.getDescricao().contains("200.00")),
                () -> assertFalse(dinheiro.permiteParcelamento()));
    }

    @ParameterizedTest
    @ValueSource(strings = { "0", "-5.00" })
    @DisplayName("Deve recusar valor recebido zero ou negativo")
    void deveRecusarValorRecebidoNaoPositivo(String valor) {
        assertThrows(IllegalArgumentException.class, () -> new Dinheiro(new BigDecimal(valor)));
    }

    @Test
    @DisplayName("Deve recusar valor recebido nulo")
    void deveRecusarValorRecebidoNulo() {
        assertThrows(IllegalArgumentException.class, () -> new Dinheiro(null));
    }
}
