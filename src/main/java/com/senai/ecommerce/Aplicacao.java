package com.senai.ecommerce;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import com.senai.ecommerce.excecao.ECommerceException;
import com.senai.ecommerce.excecao.EstoqueInsuficienteException;
import com.senai.ecommerce.excecao.PagamentoRecusadoException;
import com.senai.ecommerce.excecao.PedidoInvalidoException;
import com.senai.ecommerce.modelo.Cliente;
import com.senai.ecommerce.modelo.Endereco;
import com.senai.ecommerce.modelo.Pedido;
import com.senai.ecommerce.modelo.Produto;
import com.senai.ecommerce.modelo.pagamento.Boleto;
import com.senai.ecommerce.modelo.pagamento.CartaoCredito;
import com.senai.ecommerce.modelo.pagamento.Dinheiro;
import com.senai.ecommerce.modelo.pagamento.Pix;
import com.senai.ecommerce.modelo.pagamento.ProcessadorPagamento;

public class Aplicacao {

    public static void main(String[] args) {
        Produto monitor = new Produto("cod001", "Monitor", 2000.00, 20);
        monitor.setDescricao("30 polegadas");

        Produto teclado = new Produto("cod002", "Teclado", 30.00, 2000);
        teclado.setDescricao("RGB");

        System.out.println(monitor);
        System.out.println(teclado);

        Endereco endereco = new Endereco("13560-000", "Rua das Flores", "100", "Centro", "São Carlos", "SP");
        Cliente cliente = new Cliente("Ana Souza", "123.456.789-00", "ana@email.com", endereco);

        System.out.println();
        System.out.println("=== Aula 08 (recapitulação): polimorfismo e aberto/fechado ===");
        demonstrarPolimorfismoDePagamento();

        System.out.println();
        System.out.println("=== Aula 09 — cenários de cadastro inválido ===");
        testarCadastrosInvalidos();

        System.out.println();
        System.out.println("=== Aula 09 — caminho feliz: pedido pago com Pix ===");
        Pedido pedidoFeliz = caminhoFeliz(cliente, monitor, teclado);

        System.out.println();
        System.out.println("=== Aula 09 — cenários de erro no fluxo de pedido ===");
        testarCenariosDePedido(cliente, monitor, teclado);

        System.out.println();
        System.out.println("=== Aula 09 — cenários de erro no fluxo de pagamento ===");
        testarCenariosDePagamento(cliente, teclado, pedidoFeliz);

        System.out.println();
        System.out.println("=== Aula 09 — encadeamento de causa + try-with-resources ===");
        demonstrarEncadeamentoDeCausa(pedidoFeliz);
    }

    // ===== Aula 08: recapitulação do módulo de pagamento polimórfico =====
    private static void demonstrarPolimorfismoDePagamento() {
        List<ProcessadorPagamento> formas = List.of(
                new Pix(new BigDecimal("150.00"), "cliente@email.com"),
                new Boleto(new BigDecimal("150.00"), LocalDate.now().plusDays(3)),
                new CartaoCredito(new BigDecimal("150.00"), "**** 1234", 3));

        for (ProcessadorPagamento forma : formas) {
            System.out.println("--- " + forma.getDescricao());
            boolean ok = forma.processar(new BigDecimal("150.00"));
            System.out.println(ok ? "Aprovado: " + forma.getComprovante() : "Aguardando aprovação");
        }

        ProcessadorPagamento dinheiro = new Dinheiro(new BigDecimal("200.00"));
        System.out.println("--- " + dinheiro.getDescricao() + " (extensão sem alterar nada existente)");
        dinheiro.processar(new BigDecimal("150.00"));
        System.out.println("Aprovado: " + dinheiro.getComprovante());
    }

    // ===== Aula 09: cadastro de produto e cliente =====
    private static void testarCadastrosInvalidos() {
        try {
            new Produto("cod999", "", 10.00, 5);
            System.out.println("FALHOU: aceitou produto com nome vazio");
        } catch (IllegalArgumentException e) {
            System.out.println("OK: recusou nome vazio -> " + e.getMessage());
        }

        try {
            new Produto("cod999", "Mouse", -10.00, 5);
            System.out.println("FALHOU: aceitou preço negativo");
        } catch (IllegalArgumentException e) {
            System.out.println("OK: recusou preço negativo -> " + e.getMessage());
        }

        try {
            new Produto("cod999", "Mouse", 0.00, 5);
            System.out.println("FALHOU: aceitou preço zero");
        } catch (IllegalArgumentException e) {
            System.out.println("OK: recusou preço zero -> " + e.getMessage());
        }

        try {
            new Produto("cod999", "Mouse", 50.00, -3);
            System.out.println("FALHOU: aceitou estoque negativo");
        } catch (IllegalArgumentException e) {
            System.out.println("OK: recusou estoque negativo -> " + e.getMessage());
        }

        Endereco enderecoTeste = new Endereco("13560-000", "Rua X", "1", "Centro", "São Carlos", "SP");

        try {
            new Cliente("", "123.456.789-00", "ana@email.com", enderecoTeste);
            System.out.println("FALHOU: aceitou cliente com nome vazio");
        } catch (IllegalArgumentException e) {
            System.out.println("OK: recusou nome vazio -> " + e.getMessage());
        }

        try {
            new Cliente("Ana", "123.456.789-00", "ana-email.com", enderecoTeste);
            System.out.println("FALHOU: aceitou e-mail sem @");
        } catch (IllegalArgumentException e) {
            System.out.println("OK: recusou e-mail inválido -> " + e.getMessage());
        }

        try {
            new Cliente("Ana", "123", "ana@email.com", enderecoTeste);
            System.out.println("FALHOU: aceitou CPF inválido");
        } catch (IllegalArgumentException e) {
            System.out.println("OK: recusou CPF inválido -> " + e.getMessage());
        }
    }

    // ===== Aula 09: caminho feliz (validação apressada costuma barrar o que é válido) =====
    private static Pedido caminhoFeliz(Cliente cliente, Produto monitor, Produto teclado) {
        try {
            Pedido pedido = new Pedido("PED-0001", cliente);
            pedido.adicionarItem(monitor, 2);
            pedido.adicionarItem(teclado); // sobrecarga: 1 unidade por padrão
            System.out.println(pedido);
            System.out.println("Total do pedido: R$ " + pedido.calcularValorTotal());

            pedido.pagar(new Pix(pedido.calcularValorTotal(), "ana@email.com"));
            System.out.println("OK: caminho feliz -> " + pedido);
            return pedido;
        } catch (EstoqueInsuficienteException | PedidoInvalidoException | PagamentoRecusadoException e) {
            // não deveria acontecer aqui; se acontecer, a validação está barrando
            // também o que é válido — e isso é um defeito a corrigir, não um
            // cenário de negócio
            throw new IllegalStateException("Caminho feliz não deveria falhar: " + e.getMessage(), e);
        }
    }

    // ===== Aula 09: fluxo de pedido =====
    private static void testarCenariosDePedido(Cliente cliente, Produto monitor, Produto teclado) {
        Pedido pedido = new Pedido("PED-0002", cliente);

        try {
            pedido.adicionarItem(null, 1);
            System.out.println("FALHOU: aceitou produto null");
        } catch (IllegalArgumentException e) {
            System.out.println("OK: recusou produto null -> " + e.getMessage());
        } catch (EstoqueInsuficienteException | PedidoInvalidoException e) {
            System.out.println("FALHOU: exceção inesperada -> " + e.getMessage());
        }

        try {
            pedido.adicionarItem(monitor, 0);
            System.out.println("FALHOU: aceitou quantidade zero");
        } catch (IllegalArgumentException e) {
            System.out.println("OK: recusou quantidade zero -> " + e.getMessage());
        } catch (EstoqueInsuficienteException | PedidoInvalidoException e) {
            System.out.println("FALHOU: exceção inesperada -> " + e.getMessage());
        }

        try {
            pedido.adicionarItem(monitor, 9999);
            System.out.println("FALHOU: aceitou quantidade maior que o estoque");
        } catch (EstoqueInsuficienteException e) {
            System.out.println("OK: recusou estoque insuficiente -> " + e.getMessage());
            System.out.println("    Disponível agora: " + e.getProduto().getQuantidadeEmEstoque());
        } catch (PedidoInvalidoException e) {
            System.out.println("FALHOU: exceção inesperada -> " + e.getMessage());
        }

        try {
            pedido.getItens().clear();
            System.out.println("FALHOU: permitiu alterar a lista de itens por fora");
        } catch (UnsupportedOperationException e) {
            System.out.println("OK: getItens() devolve lista protegida contra alteração externa");
        }

        // item válido, para depois pagar e tentar alterar um pedido já pago
        try {
            pedido.adicionarItem(teclado, 2);
            pedido.pagar(new Pix(pedido.calcularValorTotal(), "ana@email.com"));
        } catch (EstoqueInsuficienteException | PedidoInvalidoException | PagamentoRecusadoException e) {
            System.out.println("FALHOU ao preparar o cenário -> " + e.getMessage());
        }

        try {
            pedido.adicionarItem(teclado, 1);
            System.out.println("FALHOU: alterou um pedido já pago");
        } catch (PedidoInvalidoException e) {
            System.out.println("OK: recusou alterar pedido já pago -> " + e.getMessage());
        } catch (EstoqueInsuficienteException e) {
            System.out.println("FALHOU: exceção inesperada -> " + e.getMessage());
        }
    }

    // ===== Aula 09: fluxo de pagamento =====
    private static void testarCenariosDePagamento(Cliente cliente, Produto teclado, Pedido pedidoJaPago) {
        try {
            pedidoJaPago.pagar(null);
            System.out.println("FALHOU: aceitou processador null");
        } catch (IllegalArgumentException e) {
            System.out.println("OK: recusou processador null -> " + e.getMessage());
        } catch (PedidoInvalidoException | PagamentoRecusadoException e) {
            System.out.println("FALHOU: exceção inesperada -> " + e.getMessage());
        }

        try {
            Pedido pedidoVazio = new Pedido("PED-0003", cliente);
            pedidoVazio.pagar(new Pix(new BigDecimal("10.00"), "ana@email.com"));
            System.out.println("FALHOU: pagou pedido sem itens");
        } catch (PedidoInvalidoException e) {
            System.out.println("OK: recusou pagar pedido sem itens -> " + e.getMessage());
        } catch (PagamentoRecusadoException e) {
            System.out.println("FALHOU: exceção inesperada -> " + e.getMessage());
        }

        try {
            pedidoJaPago.pagar(new Pix(pedidoJaPago.calcularValorTotal(), "ana@email.com"));
            System.out.println("FALHOU: pagou pedido que já estava pago");
        } catch (PedidoInvalidoException | PagamentoRecusadoException e) {
            // multi-catch: as duas situações recebem exatamente o mesmo tratamento
            // aqui — informar o cliente e confirmar que a situação não mudou
            System.out.println("OK: pagamento não realizado -> " + e.getMessage()
                    + " (situação continua " + pedidoJaPago.getSituacao() + ")");
        }

        try {
            Pedido pedidoComTroco = new Pedido("PED-0004", cliente);
            pedidoComTroco.adicionarItem(teclado, 1);
            pedidoComTroco.pagar(new Dinheiro(new BigDecimal("5.00"))); // bem menos que o total
            System.out.println("FALHOU: aprovou pagamento em dinheiro insuficiente");
        } catch (PagamentoRecusadoException e) {
            System.out.println("OK: pagamento recusado -> " + e.getMessage());
        } catch (EstoqueInsuficienteException | PedidoInvalidoException e) {
            System.out.println("FALHOU: exceção inesperada -> " + e.getMessage());
        }

        try {
            new CartaoCredito(new BigDecimal("150.00"), "**** 1234", 15);
            System.out.println("FALHOU: aceitou cartão com 15 parcelas");
        } catch (IllegalArgumentException e) {
            System.out.println("OK: recusou cartão com mais de 12 parcelas -> " + e.getMessage());
        }
    }

    /**
     * Item desejável: encadeamento de causa + try-with-resources.
     * Traduz uma falha de baixo nível (entrada não numérica) para a
     * linguagem do domínio sem perder o rastro original, e mostra a
     * raiz {@link ECommerceException} funcionando como rede de
     * segurança da camada de apresentação.
     */
    private static void demonstrarEncadeamentoDeCausa(Pedido pedido) {
        String entradaDoUsuario = "dez"; // simula um usuário digitando um texto em vez de um número
        try (Scanner leitor = new Scanner(entradaDoUsuario)) {
            int quantidade = Integer.parseInt(leitor.next());
            System.out.println("Quantidade lida: " + quantidade);
        } catch (NumberFormatException causaOriginal) {
            try {
                throw new PedidoInvalidoException(pedido.getNumero(),
                        "quantidade informada pelo usuário não é um número", causaOriginal);
            } catch (PedidoInvalidoException traduzida) {
                System.out.println("OK: entrada inválida traduzida -> " + traduzida.getMessage());
                System.out.println("    Causa original preservada -> " + traduzida.getCause());
            }
        }

        // rede de segurança da camada de apresentação: qualquer exceção de
        // negócio que tenha escapado dos tratamentos específicos cai aqui
        try {
            pedido.pagar(new Pix(pedido.calcularValorTotal(), "ana@email.com"));
        } catch (ECommerceException e) {
            System.out.println("Não foi possível concluir: " + e.getMessage());
        }
    }
}
