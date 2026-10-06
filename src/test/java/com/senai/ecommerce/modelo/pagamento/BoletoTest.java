package com.senai.ecommerce.modelo.pagamento;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Boleto")
class BoletoTest {

    private static final BigDecimal VALOR = new BigDecimal("150.00");

    private LocalDate vencimento;
    private Boleto boleto;

    @BeforeEach
    void prepararCenario() {
        vencimento = LocalDate.now().plusDays(3);
        boleto = new Boleto(VALOR, vencimento);
    }

    @Test
    @DisplayName("Deve criar boleto com valor e vencimento quando os dados são válidos")
    void deveCriarBoletoQuandoDadosValidos() {
        assertAll(
                () -> assertEquals(0, VALOR.compareTo(boleto.getValor())),
                () -> assertEquals(vencimento, boleto.getVencimento()));
    }

    @Test
    @DisplayName("Deve aceitar vencimento no dia de hoje")
    void deveAceitarVencimentoHoje() {
        Boleto venceHoje = new Boleto(VALOR, LocalDate.now());
        assertEquals(LocalDate.now(), venceHoje.getVencimento());
    }

    @Test
    @DisplayName("Deve ficar aguardando compensação e não gerar comprovante quando processado")
    void deveFicarAguardandoCompensacao() {
        boolean aprovado = boleto.processar(VALOR);

        assertAll(
                () -> assertFalse(aprovado),
                () -> assertNull(boleto.getComprovante()),
                () -> assertNull(boleto.getDataDoPagamento()));
    }

    @Test
    @DisplayName("Deve incluir o vencimento na descrição do pagamento")
    void deveIncluirVencimentoNaDescricao() {
        assertTrue(boleto.getDescricao().contains("vence em " + vencimento));
    }

    @Test
    @DisplayName("Deve informar que não permite parcelamento")
    void deveInformarQueNaoPermiteParcelamento() {
        assertFalse(boleto.permiteParcelamento());
    }

    @Test
    @DisplayName("Deve recusar boleto quando o vencimento é no passado")
    void deveRecusarVencimentoNoPassado() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> new Boleto(VALOR, LocalDate.now().minusDays(1)));
        assertEquals("Vencimento do boleto não pode ser no passado", erro.getMessage());
    }

    @Test
    @DisplayName("Deve recusar boleto quando o vencimento é nulo")
    void deveRecusarVencimentoNulo() {
        assertThrows(IllegalArgumentException.class, () -> new Boleto(VALOR, null));
    }

    @Test
    @DisplayName("Deve recusar boleto quando o valor é zero")
    void deveRecusarValorZero() {
        assertThrows(IllegalArgumentException.class, () -> new Boleto(BigDecimal.ZERO, vencimento));
    }
}
