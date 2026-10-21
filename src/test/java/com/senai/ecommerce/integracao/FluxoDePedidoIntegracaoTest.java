package com.senai.ecommerce.integracao;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.senai.ecommerce.excecao.EstoqueInsuficienteException;
import com.senai.ecommerce.excecao.PagamentoRecusadoException;
import com.senai.ecommerce.excecao.PedidoInvalidoException;
import com.senai.ecommerce.modelo.Cliente;
import com.senai.ecommerce.modelo.Endereco;
import com.senai.ecommerce.modelo.Pedido;
import com.senai.ecommerce.modelo.Produto;
import com.senai.ecommerce.modelo.SituacaoDoPedido;
import com.senai.ecommerce.modelo.pagamento.Boleto;
import com.senai.ecommerce.modelo.pagamento.CartaoCredito;
import com.senai.ecommerce.modelo.pagamento.Dinheiro;
import com.senai.ecommerce.modelo.pagamento.Pix;
import com.senai.ecommerce.modelo.pagamento.ProcessadorPagamento;

/**
 * Aula 11 — testes de integração do fluxo que gera dinheiro:
 * cadastrar produto -> criar pedido -> adicionar item -> pagar ->
 * conferir estoque e situação.
 *
 * Aqui as classes do domínio são as de verdade (Produto, Cliente,
 * Pedido, ItemPedido, formas de pagamento). O único dublê é o
 * gateway de pagamento (Mockito), porque ninguém cobra um cartão real a
 * cada {@code mvn test}.
 */
@DisplayName("Integração: cadastro -> pedido -> pagamento")
class FluxoDePedidoIntegracaoTest {

    private Cliente cliente;
    private Produto monitor;
    private Produto teclado;

    @BeforeEach
    void prepararCenario() {
        // cada teste começa do zero: nenhum depende de estado deixado por outro
        cliente = new Cliente("Ana Souza", "123.456.789-00", "ana@email.com",
                new Endereco("13560-000", "Rua das Flores", "100", "Centro", "São Carlos", "SP"));
        monitor = new Produto("MON-001", "Monitor", 2000.00, 20);
        teclado = new Produto("TEC-001", "Teclado", 30.00, 2000);
    }

    private static void assertValor(String esperado, BigDecimal obtido) {
        assertEquals(0, new BigDecimal(esperado).compareTo(obtido),
                "Esperado " + esperado + " mas foi " + obtido);
    }

    @Nested
    @DisplayName("Caminho feliz com as classes de verdade")
    class CaminhoFeliz {

        @Test
        @DisplayName("cadastrar -> criar pedido -> adicionar item -> pagar com Pix -> estoque e situação")
        void deveCumprirOFluxoCompletoComPix() throws Exception {
            Pedido pedido = new Pedido("PED-0001", cliente);
            pedido.adicionarItem(monitor, 2);
            pedido.adicionarItem(teclado);

            pedido.pagar(new Pix(pedido.calcularValorTotal(), "ana@email.com"));

            assertAll(
                    () -> assertEquals(SituacaoDoPedido.PAGO, pedido.getSituacao()),
                    () -> assertEquals(18, monitor.getQuantidadeEmEstoque(), "estoque do monitor deve baixar 2"),
                    () -> assertEquals(1999, teclado.getQuantidadeEmEstoque(), "estoque do teclado deve baixar 1"),
                    () -> assertValor("4030.00", pedido.calcularValorTotal()),
                    () -> assertNotNull(pedido.getFormaPagamento().getComprovante(),
                            "pagamento aprovado precisa gerar comprovante"));
        }

        @Test
        @DisplayName("pedido pago em dinheiro calcula o troco sobre o total do pedido")
        void devePagarEmDinheiroComTroco() throws Exception {
            Pedido pedido = new Pedido("PED-0002", cliente);
            pedido.adicionarItem(teclado, 3); // 90,00
            Dinheiro dinheiro = new Dinheiro(new BigDecimal("100.00"));

            pedido.pagar(dinheiro);

            assertEquals(SituacaoDoPedido.PAGO, pedido.getSituacao());
            assertValor("10.00", dinheiro.getTroco());
        }

        @Test
        @DisplayName("pedido pago com cartão em 3 parcelas fica PAGO e guarda a forma usada")
        void devePagarComCartao() throws Exception {
            Pedido pedido = new Pedido("PED-0003", cliente);
            pedido.adicionarItem(monitor);
            CartaoCredito cartao = new CartaoCredito(pedido.calcularValorTotal(), "**** 1234", 3);

            pedido.pagar(cartao);

            assertEquals(SituacaoDoPedido.PAGO, pedido.getSituacao());
            assertTrue(pedido.getFormaPagamento().permiteParcelamento());
        }
    }

    @Nested
    @DisplayName("O total cobrado é o total que o pedido calculou (gateway dublê)")
    class TotalCobrado {

        @Test
        @DisplayName("o gateway recebe exatamente o total calculado pelos itens")
        void deveCobrarOTotalDoPedido() throws Exception {
            ProcessadorPagamento gateway = mock(ProcessadorPagamento.class);
            when(gateway.processar(any())).thenReturn(true);
            when(gateway.getDescricao()).thenReturn("Gateway simulado");

            Pedido pedido = new Pedido("PED-0004", cliente);
            pedido.adicionarItem(monitor, 2);
            pedido.adicionarItem(teclado);

            pedido.pagar(gateway);

            // verify: a cobrança foi feita uma vez, com o valor certo
            verify(gateway).processar(pedido.calcularValorTotal());
            assertValor("4030.00", pedido.calcularValorTotal());
            assertEquals(SituacaoDoPedido.PAGO, pedido.getSituacao());
        }

        @Test
        @DisplayName("gateway recusa: pedido permanece ABERTO e a exceção traz a forma de pagamento")
        void deveManterAbertoQuandoGatewayRecusa() throws Exception {
            ProcessadorPagamento gateway = mock(ProcessadorPagamento.class);
            when(gateway.processar(any())).thenReturn(false);
            when(gateway.getDescricao()).thenReturn("Gateway simulado");

            Pedido pedido = new Pedido("PED-0005", cliente);
            pedido.adicionarItem(teclado, 2);

            PagamentoRecusadoException e = assertThrows(PagamentoRecusadoException.class,
                    () -> pedido.pagar(gateway));

            assertAll(
                    () -> assertEquals("Gateway simulado", e.getFormaPagamento()),
                    () -> assertEquals(SituacaoDoPedido.ABERTO, pedido.getSituacao()),
                    () -> assertNull(pedido.getFormaPagamento(), "recusa não pode registrar a forma de pagamento"));
        }

        @Test
        @DisplayName("pedido já pago não cobra o gateway uma segunda vez")
        void naoDeveCobrarDuasVezes() throws Exception {
            ProcessadorPagamento gateway = mock(ProcessadorPagamento.class);
            when(gateway.processar(any())).thenReturn(true);
            when(gateway.getDescricao()).thenReturn("Gateway simulado");

            Pedido pedido = new Pedido("PED-0006", cliente);
            pedido.adicionarItem(teclado);
            pedido.pagar(gateway);

            assertThrows(PedidoInvalidoException.class, () -> pedido.pagar(gateway));

            verify(gateway, org.mockito.Mockito.times(1)).processar(any());
        }

        @Test
        @DisplayName("pedido sem itens nunca chega a acionar o gateway")
        void naoDeveAcionarGatewayComPedidoVazio() {
            ProcessadorPagamento gateway = mock(ProcessadorPagamento.class);
            Pedido pedido = new Pedido("PED-0007", cliente);

            assertThrows(PedidoInvalidoException.class, () -> pedido.pagar(gateway));

            verify(gateway, never()).processar(any());
        }
    }

    @Nested
    @DisplayName("Estoque atravessando Produto, Pedido e ItemPedido")
    class Estoque {

        @Test
        @DisplayName("estoque insuficiente: item não entra no pedido e o estoque não muda")
        void naoDeveAlterarNadaQuandoEstoqueInsuficiente() throws Exception {
            Pedido pedido = new Pedido("PED-0008", cliente);

            EstoqueInsuficienteException e = assertThrows(EstoqueInsuficienteException.class,
                    () -> pedido.adicionarItem(monitor, 50));

            assertAll(
                    () -> assertEquals(20, e.getProduto().getQuantidadeEmEstoque()),
                    () -> assertEquals(50, e.getQuantidadeSolicitada()),
                    () -> assertEquals(20, monitor.getQuantidadeEmEstoque()),
                    () -> assertTrue(pedido.getItens().isEmpty()));
        }

        @Test
        @DisplayName("dois pedidos disputando o mesmo produto: o segundo recebe só o que sobrou")
        void devePermitirSomenteOQueSobrouParaOSegundoPedido() throws Exception {
            Pedido primeiro = new Pedido("PED-0009", cliente);
            Pedido segundo = new Pedido("PED-0010", cliente);

            primeiro.adicionarItem(monitor, 15);

            EstoqueInsuficienteException e = assertThrows(EstoqueInsuficienteException.class,
                    () -> segundo.adicionarItem(monitor, 10));

            assertAll(
                    () -> assertEquals(5, e.getProduto().getQuantidadeEmEstoque()),
                    () -> assertTrue(segundo.getItens().isEmpty()));

            segundo.adicionarItem(monitor, 5); // o que sobrou, cabe
            assertEquals(0, monitor.getQuantidadeEmEstoque());
        }

        @Test
        @DisplayName("PONTO FRÁGIL: pagamento recusado não devolve o estoque já reservado ao adicionar o item")
        void pagamentoRecusadoMantemEstoqueReservado() throws Exception {
            Pedido pedido = new Pedido("PED-0011", cliente);
            pedido.adicionarItem(monitor, 4);

            assertThrows(PagamentoRecusadoException.class,
                    () -> pedido.pagar(new Dinheiro(new BigDecimal("1.00"))));

            // documenta o comportamento atual: o estoque baixa ao ADICIONAR o item
            // (reserva), não ao pagar — e a recusa não o devolve. Quando houver
            // cancelamento de pedido, este teste precisa ser revisto.
            assertAll(
                    () -> assertEquals(SituacaoDoPedido.ABERTO, pedido.getSituacao()),
                    () -> assertEquals(16, monitor.getQuantidadeEmEstoque()));
        }

        @Test
        @DisplayName("pedido pago não aceita mais itens e o estoque do item recusado não baixa")
        void naoDeveAlterarPedidoPago() throws Exception {
            Pedido pedido = new Pedido("PED-0012", cliente);
            pedido.adicionarItem(teclado);
            pedido.pagar(new Pix(pedido.calcularValorTotal(), "ana@email.com"));
            int estoqueAntes = teclado.getQuantidadeEmEstoque();

            assertThrows(PedidoInvalidoException.class, () -> pedido.adicionarItem(teclado, 5));

            assertEquals(estoqueAntes, teclado.getQuantidadeEmEstoque());
            assertEquals(1, pedido.getItens().size());
        }
    }

    @Nested
    @DisplayName("Cadastro validado alimentando o pedido")
    class Cadastro {

        @Test
        @DisplayName("produto inválido nunca chega a existir, então nunca entra em pedido")
        void produtoInvalidoNaoNasce() {
            assertThrows(IllegalArgumentException.class, () -> new Produto("X-1", "Mouse", 0.00, 5));
            assertThrows(IllegalArgumentException.class, () -> new Produto("X-1", "", 10.00, 5));
        }

        @Test
        @DisplayName("cliente inválido nunca chega a existir, então nunca abre pedido")
        void clienteInvalidoNaoNasce() {
            Endereco endereco = new Endereco("13560-000", "Rua X", "1", "Centro", "São Carlos", "SP");

            assertThrows(IllegalArgumentException.class,
                    () -> new Cliente("Ana", "123.456.789-00", "ana-email.com", endereco));
            assertThrows(IllegalArgumentException.class, () -> new Pedido("PED-0013", null));
        }

        @Test
        @DisplayName("produto sem estoque suficiente corrigido depois volta a ser vendável")
        void produtoReabastecidoVoltaAVender() throws Exception {
            Produto raro = new Produto("RAR-001", "Item raro", 500.00, 1);
            Pedido pedido = new Pedido("PED-0014", cliente);
            assertThrows(EstoqueInsuficienteException.class, () -> pedido.adicionarItem(raro, 3));

            raro.setQuantidadeEmEstoque(3);
            pedido.adicionarItem(raro, 3);

            assertValor("1500.00", pedido.calcularValorTotal());
            assertEquals(0, raro.getQuantidadeEmEstoque());
        }
    }

    @Nested
    @DisplayName("Polimorfismo no fluxo: o Pedido não conhece a forma concreta")
    class Polimorfismo {

        @Test
        @DisplayName("um único laço paga um pedido por forma e cada uma se comporta à sua maneira")
        void cadaFormaSeComportaAoSeuJeito() throws Exception {
            int aprovados = 0;
            int recusados = 0;

            List<String> chaves = List.of("pix", "cartao", "dinheiro", "boleto");
            for (String tipo : chaves) {
                Pedido pedido = new Pedido("PED-" + tipo, cliente);
                pedido.adicionarItem(teclado, 2); // 60,00
                BigDecimal total = pedido.calcularValorTotal();

                ProcessadorPagamento forma = switch (tipo) {
                    case "pix" -> new Pix(total, "ana@email.com");
                    case "cartao" -> new CartaoCredito(total, "**** 9999", 2);
                    case "dinheiro" -> new Dinheiro(new BigDecimal("100.00"));
                    default -> new Boleto(total, LocalDate.now().plusDays(3));
                };

                try {
                    pedido.pagar(forma);
                    aprovados++;
                    assertEquals(SituacaoDoPedido.PAGO, pedido.getSituacao());
                } catch (PagamentoRecusadoException e) {
                    recusados++;
                    assertEquals(SituacaoDoPedido.ABERTO, pedido.getSituacao());
                }
            }

            // Pix, cartão e dinheiro aprovam na hora; boleto aguarda compensação
            assertEquals(3, aprovados);
            assertEquals(1, recusados);
        }
    }
}
