package com.senai.ecommerce.excecao;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.senai.ecommerce.modelo.Produto;

@DisplayName("Exceções do domínio")
class ExcecoesDoDominioTest {

    @Test
    @DisplayName("Deve herdar toda exceção de negócio da raiz checked ECommerceException")
    void deveHerdarDaRaizChecked() {
        Produto produto = new Produto("MS-001", "Mouse", 50.00, 1);

        assertAll(
                () -> assertInstanceOf(ECommerceException.class, new EstoqueInsuficienteException(produto, 2)),
                () -> assertInstanceOf(ECommerceException.class, new PagamentoRecusadoException("Pix", "x")),
                () -> assertInstanceOf(ECommerceException.class, new PedidoInvalidoException("PED-1", "x")),
                // checked: não pode ser RuntimeException, senão o compilador deixa de exigir tratamento
                () -> assertFalse(RuntimeException.class.isAssignableFrom(ECommerceException.class)));
    }

    @Test
    @DisplayName("Deve montar a mensagem de estoque com produto, disponível e solicitado")
    void deveMontarMensagemDeEstoque() {
        Produto produto = new Produto("MS-001", "Mouse", 50.00, 3);

        EstoqueInsuficienteException erro = new EstoqueInsuficienteException(produto, 50);

        assertEquals("Estoque insuficiente de Mouse: disponível 3, solicitado 50", erro.getMessage());
    }

    @Test
    @DisplayName("Deve carregar forma de pagamento e motivo na recusa")
    void deveCarregarFormaEMotivoNaRecusa() {
        PagamentoRecusadoException erro = new PagamentoRecusadoException("Cartão", "limite excedido");

        assertAll(
                () -> assertEquals("Cartão", erro.getFormaPagamento()),
                () -> assertEquals("limite excedido", erro.getMotivo()),
                () -> assertEquals("Pagamento por Cartão não aprovado: limite excedido", erro.getMessage()));
    }

    @Test
    @DisplayName("Deve preservar a causa original quando a exceção de pedido é traduzida")
    void devePreservarCausaOriginal() {
        NumberFormatException causa = new NumberFormatException("dez");

        PedidoInvalidoException traduzida = new PedidoInvalidoException("PED-1", "quantidade não numérica", causa);

        assertAll(
                () -> assertSame(causa, traduzida.getCause()),
                () -> assertEquals("PED-1", traduzida.getNumeroPedido()),
                () -> assertEquals("Pedido PED-1: quantidade não numérica", traduzida.getMessage()));
    }

    @Test
    @DisplayName("Deve ficar sem causa quando a exceção de pedido é criada sem encadeamento")
    void deveFicarSemCausaSemEncadeamento() {
        assertNull(new PedidoInvalidoException("PED-1", "x").getCause());
        assertTrue(new PedidoInvalidoException("PED-1", "x").getMessage().startsWith("Pedido PED-1"));
    }
}
