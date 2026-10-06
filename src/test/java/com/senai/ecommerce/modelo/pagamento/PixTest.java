package com.senai.ecommerce.modelo.pagamento;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Pix")
class PixTest {

    private Pix pix;

    @BeforeEach
    void prepararCenario() {
        pix = new Pix(new BigDecimal("150.00"), "cliente@email.com");
    }

    @Test
    @DisplayName("Deve criar Pix com valor e chave quando os dados são válidos")
    void deveCriarPixQuandoDadosValidos() {
        assertAll(
                () -> assertEquals(0, new BigDecimal("150.00").compareTo(pix.getValor())),
                () -> assertEquals("cliente@email.com", pix.getChave()));
    }

    @Test
    @DisplayName("Deve aprovar na hora e gerar comprovante e data quando o Pix é processado")
    void deveAprovarEGerarComprovante() {
        assertNull(pix.getComprovante());
        assertNull(pix.getDataDoPagamento());

        boolean aprovado = pix.processar(new BigDecimal("150.00"));

        assertAll(
                () -> assertTrue(aprovado),
                () -> assertTrue(pix.getComprovante().startsWith("PIX-")),
                () -> assertNotNull(pix.getDataDoPagamento()));
    }

    @Test
    @DisplayName("Deve incluir a chave na descrição do pagamento")
    void deveIncluirChaveNaDescricao() {
        assertTrue(pix.getDescricao().contains("cliente@email.com"));
    }

    @Test
    @DisplayName("Deve informar que não permite parcelamento")
    void deveInformarQueNaoPermiteParcelamento() {
        assertFalse(pix.permiteParcelamento());
    }

    @Test
    @DisplayName("Deve atualizar o valor quando o novo valor é positivo")
    void deveAtualizarValor() {
        pix.setValor(new BigDecimal("99.90"));
        assertEquals(0, new BigDecimal("99.90").compareTo(pix.getValor()));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "   " })
    @DisplayName("Deve recusar Pix quando a chave é vazia")
    void deveRecusarPixComChaveVazia(String chave) {
        assertThrows(IllegalArgumentException.class, () -> new Pix(new BigDecimal("10.00"), chave));
    }

    @Test
    @DisplayName("Deve recusar Pix quando a chave é nula")
    void deveRecusarPixComChaveNula() {
        assertThrows(IllegalArgumentException.class, () -> new Pix(new BigDecimal("10.00"), null));
    }

    @ParameterizedTest
    @ValueSource(strings = { "0", "-1", "-0.01" })
    @DisplayName("Deve recusar Pix quando o valor é zero ou negativo")
    void deveRecusarPixComValorNaoPositivo(String valor) {
        assertThrows(IllegalArgumentException.class, () -> new Pix(new BigDecimal(valor), "chave"));
    }

    @Test
    @DisplayName("Deve recusar Pix quando o valor é nulo e manter o valor anterior ao alterar")
    void deveRecusarValorNulo() {
        assertThrows(IllegalArgumentException.class, () -> new Pix(null, "chave"));
        assertThrows(IllegalArgumentException.class, () -> pix.setValor(null));
        assertEquals(0, new BigDecimal("150.00").compareTo(pix.getValor()));
    }
}
