package com.senai.ecommerce;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

        System.out.println(teclado.temEstoqueDisponivel(1500));
        System.out.println(monitor.temEstoqueDisponivel(2000));

        teclado.baixarEstoque(200);

        System.out.println(monitor.toString());
        System.out.println(teclado.toString());

      
        Endereco endereco = new Endereco("13560-000", "Rua das Flores", "100", "Centro", "São Carlos", "SP");
        Cliente cliente = new Cliente("Ana Souza", "123.456.789-00", "ana@email.com", endereco);

        Pedido pedido = new Pedido("PED-0001", cliente);
        pedido.adicionarItem(monitor, 2);
        pedido.adicionarItem(teclado); 

        System.out.println(pedido);
        System.out.println("Total do pedido: R$ " + pedido.calcularValorTotal());

     
        pedido.pagar(new Pix(pedido.calcularValorTotal(), "ana@email.com"));
        System.out.println(pedido);

        System.out.println();
        System.out.println("=== Momento 2, Passo 6 — o mesmo laço, comportamentos diferentes ===");
        List<ProcessadorPagamento> formas = List.of(
                new Pix(new BigDecimal("150.00"), "cliente@email.com"),
                new Boleto(new BigDecimal("150.00"), LocalDate.now().plusDays(3)),
                new CartaoCredito(new BigDecimal("150.00"), "**** 1234", 3));

        for (ProcessadorPagamento forma : formas) {
            System.out.println("--- " + forma.getDescricao());
            boolean ok = forma.processar(new BigDecimal("150.00"));
            System.out.println(ok ? "Aprovado: " + forma.getComprovante() : "Aguardando aprovação");
        }

        System.out.println();
        System.out.println("=== Momento 2, Passo 7 — a prova do princípio aberto/fechado ===");
        System.out.println("Acrescentando Dinheiro sem alterar Pedido, Pix, Boleto ou CartaoCredito:");
        ProcessadorPagamento dinheiro = new Dinheiro(new BigDecimal("200.00"));
        System.out.println("--- " + dinheiro.getDescricao());
        boolean pagouEmDinheiro = dinheiro.processar(new BigDecimal("150.00"));
        System.out.println(pagouEmDinheiro ? "Aprovado: " + dinheiro.getComprovante() : "Recusado");

     
        try {
            new Pedido("PED-0002", null);
            System.out.println("FALHOU: aceitou pedido sem cliente");
        } catch (IllegalArgumentException e) {
            System.out.println("OK: recusou pedido sem cliente -> " + e.getMessage());
        }

        try {
            pedido.adicionarItem(null, 1);
            System.out.println("FALHOU: aceitou item com produto null");
        } catch (IllegalArgumentException e) {
            System.out.println("OK: recusou item com produto null -> " + e.getMessage());
        }

        try {
            pedido.adicionarItem(monitor, 0);
            System.out.println("FALHOU: aceitou item com quantidade zero");
        } catch (IllegalArgumentException e) {
            System.out.println("OK: recusou item com quantidade zero -> " + e.getMessage());
        }

        try {
            pedido.adicionarItem(monitor, 9999);
            System.out.println("FALHOU: aceitou quantidade maior que o estoque");
        } catch (IllegalStateException e) {
            System.out.println("OK: recusou quantidade maior que o estoque -> " + e.getMessage());
        }

        try {
            Pedido pedidoVazio = new Pedido("PED-0003", cliente);
            pedidoVazio.pagar(new Pix(new BigDecimal("10.00"), "ana@email.com"));
            System.out.println("FALHOU: pagou pedido sem itens");
        } catch (IllegalStateException e) {
            System.out.println("OK: recusou pagar pedido sem itens -> " + e.getMessage());
        }

        try {
            pedido.getItens().clear();
            System.out.println("FALHOU: permitiu alterar a lista de itens por fora");
        } catch (UnsupportedOperationException e) {
            System.out.println("OK: getItens() devolve lista protegida contra alteração externa");
        }

       
        System.out.println();
        System.out.println("=== Testes funcionais do fluxo de pagamento ===");

        try {
            pedido.pagar(null);
            System.out.println("FALHOU: aceitou processador null");
        } catch (IllegalArgumentException e) {
            System.out.println("OK: recusou processador null -> " + e.getMessage());
        }

        try {
            pedido.pagar(new Pix(pedido.calcularValorTotal(), "ana@email.com"));
            System.out.println("FALHOU: pagou pedido que já estava pago");
        } catch (IllegalStateException e) {
            System.out.println("OK: recusou pagar pedido já pago -> " + e.getMessage());
        }

        try {
            new CartaoCredito(new BigDecimal("150.00"), "**** 1234", 15);
            System.out.println("FALHOU: aceitou cartão com 15 parcelas");
        } catch (IllegalArgumentException e) {
            System.out.println("OK: recusou cartão com mais de 12 parcelas -> " + e.getMessage());
        }

        Pedido pedidoBoleto = new Pedido("PED-0004", cliente);
        pedidoBoleto.adicionarItem(teclado, 2);
        boolean boletoAprovado = pedidoBoleto.pagar(
                new Boleto(pedidoBoleto.calcularValorTotal(), LocalDate.now().plusDays(5)));
        System.out.println(boletoAprovado
                ? "FALHOU: boleto aprovou na hora"
                : "OK: boleto aguardando compensação, situação = " + pedidoBoleto.getSituacao());
    }
}
