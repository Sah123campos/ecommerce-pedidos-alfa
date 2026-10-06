package com.senai.ecommerce.modelo;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("ItemPedido")
class ItemPedidoTest {

    private Produto teclado;

    @BeforeEach
    void prepararCenario() {
        teclado = new Produto("TEC-001", "Teclado", 30.00, 100);
    }

    @Test
    @DisplayName("Deve criar item com produto, quantidade e preço quando os dados são válidos")
    void deveCriarItemQuandoDadosValidos() {
        ItemPedido item = new ItemPedido(teclado, 2, 30.00);

        assertAll(
                () -> assertSame(teclado, item.getProduto()),
                () -> assertEquals(2, item.getQuantidade()),
                () -> assertEquals(30.00, item.getPrecoPraticado(), 0.0001));
    }

    @Test
    @DisplayName("Deve calcular o subtotal quando a quantidade é um")
    void deveCalcularSubtotalComUmaUnidade() {
        assertEquals(30.00, new ItemPedido(teclado, 1, 30.00).calcularSubtotal(), 0.0001);
    }

    @Test
    @DisplayName("Deve calcular o subtotal como preço vezes quantidade quando a quantidade é maior que um")
    void deveCalcularSubtotalComVariasUnidades() {
        assertEquals(90.00, new ItemPedido(teclado, 3, 30.00).calcularSubtotal(), 0.0001);
    }

    @Test
    @DisplayName("Deve usar o preço praticado e não o preço atual do produto no subtotal")
    void deveUsarPrecoPraticadoNoSubtotal() {
        ItemPedido item = new ItemPedido(teclado, 2, 25.00); // promoção
        assertEquals(50.00, item.calcularSubtotal(), 0.0001);
    }

    @Test
    @DisplayName("Deve aceitar preço praticado zero quando o item é brinde")
    void deveAceitarPrecoPraticadoZero() {
        assertEquals(0.00, new ItemPedido(teclado, 1, 0.00).calcularSubtotal(), 0.0001);
    }

    @Test
    @DisplayName("Deve recusar item quando o produto é nulo")
    void deveRecusarItemComProdutoNulo() {
        assertThrows(IllegalArgumentException.class, () -> new ItemPedido(null, 1, 30.00));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -1, -10 })
    @DisplayName("Deve recusar item quando a quantidade é zero ou negativa")
    void deveRecusarItemComQuantidadeNaoPositiva(int quantidade) {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> new ItemPedido(teclado, quantidade, 30.00));
        assertEquals("A quantidade deve ser maior que zero.", erro.getMessage());
    }

    @Test
    @DisplayName("Deve recusar item quando o preço praticado é negativo")
    void deveRecusarItemComPrecoNegativo() {
        assertThrows(IllegalArgumentException.class, () -> new ItemPedido(teclado, 1, -0.01));
    }
}
