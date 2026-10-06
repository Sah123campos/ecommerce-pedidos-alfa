package com.senai.ecommerce.modelo;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.senai.ecommerce.excecao.EstoqueInsuficienteException;
import com.senai.ecommerce.excecao.PagamentoRecusadoException;
import com.senai.ecommerce.excecao.PedidoInvalidoException;
import com.senai.ecommerce.modelo.pagamento.Dinheiro;
import com.senai.ecommerce.modelo.pagamento.Pix;
import com.senai.ecommerce.modelo.pagamento.ProcessadorPagamento;

@DisplayName("Pedido")
class PedidoTest {

    private Cliente cliente;
    private Produto monitor;
    private Produto teclado;
    private Pedido pedido;

    @BeforeEach
    void prepararCenario() {
        cliente = new Cliente("Ana Souza", "123.456.789-00", "ana@email.com",
                new Endereco("13560-000", "Rua das Flores", "100", "Centro", "São Carlos", "SP"));
        monitor = new Produto("MON-001", "Monitor", 2000.00, 20);
        teclado = new Produto("TEC-001", "Teclado", 30.00, 2000);
        pedido = new Pedido("PED-0001", cliente);
    }

    /** Cenário reaproveitado: 2 monitores + 1 teclado = R$ 4030,00. */
    private Pedido criarPedidoComDoisItens() throws Exception {
        pedido.adicionarItem(monitor, 2);
        pedido.adicionarItem(teclado);
        return pedido;
    }

    /** BigDecimal compara por compareTo: 4030.0 e 4030.00 são iguais em valor. */
    private static void assertValor(String esperado, BigDecimal obtido) {
        assertEquals(0, new BigDecimal(esperado).compareTo(obtido),
                "Esperado " + esperado + " mas foi " + obtido);
    }

    /** Processador controlável: evita depender das regras de Pix/Boleto/Cartão. */
    private static class ProcessadorFalso implements ProcessadorPagamento {
        private final boolean aprova;
        private int chamadas;
        private BigDecimal ultimoValor;

        ProcessadorFalso(boolean aprova) {
            this.aprova = aprova;
        }

        @Override
        public boolean processar(BigDecimal valor) {
            chamadas++;
            ultimoValor = valor;
            return aprova;
        }

        @Override
        public String getComprovante() {
            return aprova ? "FALSO-1" : null;
        }

        @Override
        public String getDescricao() {
            return "Processador falso";
        }
    }

    @Nested
    @DisplayName("Criação")
    class Criacao {

        @Test
        @DisplayName("Deve nascer aberto, sem itens, sem forma de pagamento e com total zero")
        void deveNascerAbertoESemItens() {
            assertAll(
                    () -> assertEquals("PED-0001", pedido.getNumero()),
                    () -> assertSame(cliente, pedido.getCliente()),
                    () -> assertEquals(SituacaoDoPedido.ABERTO, pedido.getSituacao()),
                    () -> assertTrue(pedido.getItens().isEmpty()),
                    () -> assertNull(pedido.getFormaPagamento()),
                    () -> assertValor("0", pedido.calcularValorTotal()));
        }

        @Test
        @DisplayName("Deve recusar pedido quando o cliente é nulo")
        void deveRecusarPedidoSemCliente() {
            IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                    () -> new Pedido("PED-0002", null));
            assertEquals("Pedido exige um cliente", erro.getMessage());
        }
    }

    @Nested
    @DisplayName("Itens e total")
    class ItensETotal {

        @Test
        @DisplayName("Deve baixar o estoque e registrar o item com o preço atual do produto")
        void deveBaixarEstoqueERegistrarItem() throws Exception {
            pedido.adicionarItem(monitor, 2);

            ItemPedido item = pedido.getItens().get(0);
            assertAll(
                    () -> assertEquals(1, pedido.getItens().size()),
                    () -> assertSame(monitor, item.getProduto()),
                    () -> assertEquals(2, item.getQuantidade()),
                    () -> assertEquals(2000.00, item.getPrecoPraticado(), 0.0001),
                    () -> assertEquals(18, monitor.getQuantidadeEmEstoque()));
        }

        @Test
        @DisplayName("Deve adicionar uma unidade quando a quantidade não é informada")
        void deveAdicionarUmaUnidadePorPadrao() throws Exception {
            pedido.adicionarItem(teclado);

            assertEquals(1, pedido.getItens().get(0).getQuantidade());
            assertEquals(1999, teclado.getQuantidadeEmEstoque());
        }

        @Test
        @DisplayName("Deve calcular o total quando há um único item")
        void deveCalcularTotalComUmItem() throws Exception {
            pedido.adicionarItem(monitor, 3);
            assertValor("6000.00", pedido.calcularValorTotal());
        }

        @Test
        @DisplayName("Deve somar o total corretamente quando há dois itens")
        void deveSomarTotalComDoisItens() throws Exception {
            criarPedidoComDoisItens();
            assertValor("4030.00", pedido.calcularValorTotal());
        }

        @Test
        @DisplayName("Deve manter o total quando o preço do produto muda depois da inclusão")
        void deveManterTotalQuandoPrecoDoProdutoMuda() throws Exception {
            criarPedidoComDoisItens();
            monitor.setPreco(2500.00);
            assertValor("4030.00", pedido.calcularValorTotal());
        }

        @Test
        @DisplayName("Deve recusar item quando o produto é nulo")
        void deveRecusarItemComProdutoNulo() {
            IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                    () -> pedido.adicionarItem(null, 1));
            assertEquals("Produto é obrigatório", erro.getMessage());
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1 })
        @DisplayName("Deve recusar item quando a quantidade é zero ou negativa, sem mexer em estoque nem itens")
        void deveRecusarItemComQuantidadeNaoPositiva(int quantidade) {
            assertThrows(IllegalArgumentException.class, () -> pedido.adicionarItem(monitor, quantidade));
            assertAll(
                    () -> assertEquals(20, monitor.getQuantidadeEmEstoque()),
                    () -> assertTrue(pedido.getItens().isEmpty()));
        }

        @Test
        @DisplayName("Deve lançar exceção e não registrar o item quando o estoque é insuficiente")
        void deveLancarExcecaoQuandoEstoqueInsuficiente() {
            EstoqueInsuficienteException erro = assertThrows(EstoqueInsuficienteException.class,
                    () -> pedido.adicionarItem(monitor, 9999));

            assertAll(
                    () -> assertSame(monitor, erro.getProduto()),
                    () -> assertEquals(9999, erro.getQuantidadeSolicitada()),
                    () -> assertTrue(pedido.getItens().isEmpty()),
                    () -> assertEquals(20, monitor.getQuantidadeEmEstoque()));
        }

        @Test
        @DisplayName("Deve proteger a lista de itens contra alteração externa")
        void deveProtegerListaDeItens() throws Exception {
            criarPedidoComDoisItens();
            assertThrows(UnsupportedOperationException.class, () -> pedido.getItens().clear());
            assertEquals(2, pedido.getItens().size());
        }
    }

    @Nested
    @DisplayName("Pagamento de pedido aberto")
    class PagamentoDePedidoAberto {

        @Test
        @DisplayName("Deve mudar para PAGO e guardar a forma de pagamento quando o pagamento é aprovado")
        void devePagarPedidoComPixESituacaoFicarPaga() throws Exception {
            criarPedidoComDoisItens();
            Pix pix = new Pix(pedido.calcularValorTotal(), "ana@email.com");

            pedido.pagar(pix);

            assertAll(
                    () -> assertEquals(SituacaoDoPedido.PAGO, pedido.getSituacao()),
                    () -> assertSame(pix, pedido.getFormaPagamento()));
        }

        @Test
        @DisplayName("Deve enviar o total do pedido ao processador de pagamento")
        void deveEnviarTotalAoProcessador() throws Exception {
            criarPedidoComDoisItens();
            ProcessadorFalso processador = new ProcessadorFalso(true);

            pedido.pagar(processador);

            assertValor("4030.00", processador.ultimoValor);
        }

        @Test
        @DisplayName("Deve recusar o pagamento quando o processador é nulo, sem mudar a situação")
        void deveRecusarProcessadorNulo() throws Exception {
            criarPedidoComDoisItens();

            assertThrows(IllegalArgumentException.class, () -> pedido.pagar(null));
            assertEquals(SituacaoDoPedido.ABERTO, pedido.getSituacao());
        }

        @Test
        @DisplayName("Deve lançar exceção quando o pedido não tem itens, mantendo-o aberto")
        void deveRecusarPagamentoDePedidoSemItens() {
            ProcessadorFalso processador = new ProcessadorFalso(true);

            PedidoInvalidoException erro = assertThrows(PedidoInvalidoException.class,
                    () -> pedido.pagar(processador));

            assertAll(
                    () -> assertEquals("PED-0001", erro.getNumeroPedido()),
                    () -> assertTrue(erro.getMessage().contains("sem itens")),
                    () -> assertEquals(SituacaoDoPedido.ABERTO, pedido.getSituacao()),
                    () -> assertEquals(0, processador.chamadas));
        }

        @Test
        @DisplayName("Deve manter o pedido aberto e sem forma de pagamento quando o pagamento é recusado")
        void deveManterPedidoAbertoQuandoPagamentoRecusado() throws Exception {
            criarPedidoComDoisItens();

            PagamentoRecusadoException erro = assertThrows(PagamentoRecusadoException.class,
                    () -> pedido.pagar(new Dinheiro(new BigDecimal("5.00"))));

            assertAll(
                    () -> assertTrue(erro.getFormaPagamento().contains("Dinheiro")),
                    () -> assertEquals(SituacaoDoPedido.ABERTO, pedido.getSituacao()),
                    () -> assertNull(pedido.getFormaPagamento()));
        }

        @Test
        @DisplayName("Deve permitir nova tentativa com outra forma de pagamento depois de uma recusa")
        void devePermitirNovaTentativaDepoisDeRecusa() throws Exception {
            criarPedidoComDoisItens();
            assertThrows(PagamentoRecusadoException.class,
                    () -> pedido.pagar(new ProcessadorFalso(false)));

            pedido.pagar(new Pix(pedido.calcularValorTotal(), "ana@email.com"));

            assertEquals(SituacaoDoPedido.PAGO, pedido.getSituacao());
        }
    }

    @Nested
    @DisplayName("Pedido já pago")
    class PedidoJaPago {

        private ProcessadorFalso processadorUsado;

        @BeforeEach
        void pagarPedido() throws Exception {
            criarPedidoComDoisItens();
            processadorUsado = new ProcessadorFalso(true);
            pedido.pagar(processadorUsado);
        }

        @Test
        @DisplayName("Deve lançar exceção e não cobrar de novo quando se tenta pagar outra vez")
        void deveRecusarPagarPedidoJaPago() {
            ProcessadorFalso segundo = new ProcessadorFalso(true);

            PedidoInvalidoException erro = assertThrows(PedidoInvalidoException.class,
                    () -> pedido.pagar(segundo));

            assertAll(
                    () -> assertTrue(erro.getMessage().contains("já está pago")),
                    () -> assertEquals(0, segundo.chamadas),
                    () -> assertEquals(1, processadorUsado.chamadas),
                    () -> assertEquals(SituacaoDoPedido.PAGO, pedido.getSituacao()));
        }

        @Test
        @DisplayName("Deve lançar exceção e preservar estoque e itens quando se tenta alterar o pedido")
        void deveRecusarAlterarPedidoJaPago() {
            int estoqueAntes = teclado.getQuantidadeEmEstoque();

            PedidoInvalidoException erro = assertThrows(PedidoInvalidoException.class,
                    () -> pedido.adicionarItem(teclado, 1));

            assertAll(
                    () -> assertTrue(erro.getMessage().contains("já pago")),
                    () -> assertEquals(2, pedido.getItens().size()),
                    () -> assertEquals(estoqueAntes, teclado.getQuantidadeEmEstoque()));
        }
    }
}
