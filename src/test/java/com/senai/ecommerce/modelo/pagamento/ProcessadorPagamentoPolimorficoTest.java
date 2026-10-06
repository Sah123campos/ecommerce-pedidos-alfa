package com.senai.ecommerce.modelo.pagamento;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Fluxo polimórfico: quem usa {@link ProcessadorPagamento} não conhece a
 * classe concreta. Estes testes garantem que o contrato vale para todas
 * as formas — inclusive a que a equipe criar amanhã.
 */
@DisplayName("Formas de pagamento (fluxo polimórfico)")
class ProcessadorPagamentoPolimorficoTest {

    private static final BigDecimal VALOR = new BigDecimal("150.00");

    private ProcessadorPagamento pix;
    private ProcessadorPagamento boleto;
    private ProcessadorPagamento cartao;
    private ProcessadorPagamento dinheiro;
    private List<ProcessadorPagamento> todas;

    @BeforeEach
    void prepararCenario() {
        pix = new Pix(VALOR, "cliente@email.com");
        boleto = new Boleto(VALOR, LocalDate.now().plusDays(3));
        cartao = new CartaoCredito(VALOR, "**** 1234", 3);
        dinheiro = new Dinheiro(new BigDecimal("200.00"));
        todas = List.of(pix, boleto, cartao, dinheiro);
    }

    @Test
    @DisplayName("Deve aprovar na hora e devolver comprovante em Pix, cartão e dinheiro")
    void deveAprovarEDevolverComprovanteNasFormasImediatas() {
        for (ProcessadorPagamento forma : List.of(pix, cartao, dinheiro)) {
            boolean aprovado = forma.processar(VALOR);

            assertAll(forma.getClass().getSimpleName(),
                    () -> assertTrue(aprovado),
                    () -> assertNotNull(forma.getComprovante()));
        }
    }

    @Test
    @DisplayName("Deve deixar o boleto aguardando compensação, sem comprovante")
    void deveDeixarBoletoAguardando() {
        assertFalse(boleto.processar(VALOR));
        assertNull(boleto.getComprovante());
    }

    @Test
    @DisplayName("Deve oferecer uma descrição preenchida em todas as formas")
    void deveOferecerDescricaoEmTodasAsFormas() {
        for (ProcessadorPagamento forma : todas) {
            assertFalse(forma.getDescricao().isBlank(), forma.getClass().getSimpleName());
        }
    }

    @Test
    @DisplayName("Deve permitir parcelamento somente no cartão de crédito")
    void devePermitirParcelamentoSomenteNoCartao() {
        long comParcelamento = todas.stream().filter(ProcessadorPagamento::permiteParcelamento).count();

        assertAll(
                () -> assertEquals(1, comParcelamento),
                () -> assertTrue(cartao.permiteParcelamento()));
    }

    @Test
    @DisplayName("Deve ser estornável somente o cartão de crédito")
    void deveSerEstornavelSomenteOCartao() {
        long estornaveis = todas.stream().filter(forma -> forma instanceof Estornavel).count();

        assertAll(
                () -> assertEquals(1, estornaveis),
                () -> assertTrue(cartao instanceof Estornavel));
    }
}
