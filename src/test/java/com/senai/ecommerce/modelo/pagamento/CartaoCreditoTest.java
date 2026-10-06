package com.senai.ecommerce.modelo.pagamento;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("CartaoCredito")
class CartaoCreditoTest {

    private static final BigDecimal VALOR = new BigDecimal("150.00");

    private CartaoCredito cartao;

    @BeforeEach
    void prepararCenario() {
        cartao = new CartaoCredito(VALOR, "**** 1234", 3);
    }

    @Test
    @DisplayName("Deve criar cartão com as parcelas informadas quando os dados são válidos")
    void deveCriarCartaoQuandoDadosValidos() {
        assertEquals(3, cartao.getParcelas());
    }

    @Test
    @DisplayName("Deve aceitar exatamente 12 parcelas, o limite permitido")
    void deveAceitarDozeParcelas() {
        assertEquals(12, new CartaoCredito(VALOR, "**** 1234", 12).getParcelas());
    }

    @Test
    @DisplayName("Deve aceitar parcela de exatamente R$ 5,00, o mínimo permitido")
    void deveAceitarParcelaNoValorMinimo() {
        CartaoCredito noLimite = new CartaoCredito(new BigDecimal("60.00"), "**** 1234", 12);
        assertEquals(12, noLimite.getParcelas());
    }

    @Test
    @DisplayName("Deve aprovar e gerar comprovante e data quando o cartão é processado")
    void deveAprovarEGerarComprovante() {
        boolean aprovado = cartao.processar(VALOR);

        assertAll(
                () -> assertTrue(aprovado),
                () -> assertTrue(cartao.getComprovante().startsWith("CARTAO-")),
                () -> assertNotNull(cartao.getDataDoPagamento()));
    }

    @Test
    @DisplayName("Deve permitir parcelamento e ser estornável")
    void devePermitirParcelamentoESerEstornavel() {
        assertAll(
                () -> assertTrue(cartao.permiteParcelamento()),
                () -> assertInstanceOf(Estornavel.class, cartao),
                () -> assertTrue(cartao.estornar(VALOR)));
    }

    @Test
    @DisplayName("Deve incluir número mascarado e parcelas na descrição do pagamento")
    void deveIncluirNumeroEParcelasNaDescricao() {
        assertTrue(cartao.getDescricao().contains("**** 1234, 3x"));
    }

    @ParameterizedTest
    @ValueSource(ints = { 13, 15, 100 })
    @DisplayName("Deve recusar cartão quando as parcelas passam de 12")
    void deveRecusarMaisDeDozeParcelas(int parcelas) {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> new CartaoCredito(VALOR, "**** 1234", parcelas));
        assertEquals("Cartão não aceita mais de 12 parcelas", erro.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -1 })
    @DisplayName("Deve recusar cartão quando as parcelas são zero ou negativas")
    void deveRecusarParcelasNaoPositivas(int parcelas) {
        assertThrows(IllegalArgumentException.class, () -> new CartaoCredito(VALOR, "**** 1234", parcelas));
    }

    @Test
    @DisplayName("Deve recusar cartão quando o valor por parcela fica abaixo de R$ 5,00")
    void deveRecusarParcelaAbaixoDoMinimo() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> new CartaoCredito(new BigDecimal("50.00"), "**** 1234", 12));
        assertTrue(erro.getMessage().startsWith("Valor por parcela abaixo do mínimo"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "  " })
    @DisplayName("Deve recusar cartão quando o número é vazio")
    void deveRecusarNumeroVazio(String numero) {
        assertThrows(IllegalArgumentException.class, () -> new CartaoCredito(VALOR, numero, 1));
    }

    @Test
    @DisplayName("Deve recusar cartão quando o valor é nulo ou não positivo")
    void deveRecusarValorInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new CartaoCredito(null, "**** 1234", 1));
        assertThrows(IllegalArgumentException.class,
                () -> new CartaoCredito(new BigDecimal("-10"), "**** 1234", 1));
    }
}
