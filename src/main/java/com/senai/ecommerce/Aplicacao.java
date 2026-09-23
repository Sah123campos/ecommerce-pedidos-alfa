package com.senai.ecommerce;

import java.math.BigDecimal;

import com.senai.ecommerce.modelo.Cliente;
import com.senai.ecommerce.modelo.Endereco;
import com.senai.ecommerce.modelo.Pedido;
import com.senai.ecommerce.modelo.Produto;
import com.senai.ecommerce.modelo.pagamento.Pix;

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

        // ===== Aula 07: modelo de domínio completo (relacionamentos) =====
        Endereco endereco = new Endereco("13560-000", "Rua das Flores", "100", "Centro", "São Carlos", "SP");
        Cliente cliente = new Cliente("Ana Souza", "123.456.789-00", "ana@email.com", endereco);

        Pedido pedido = new Pedido("PED-0001", cliente);
        pedido.adicionarItem(monitor, 2);
        pedido.adicionarItem(teclado, 3);

        System.out.println(pedido);
        System.out.println("Total do pedido: R$ " + pedido.calcularValorTotal());

        pedido.pagarCom(new Pix(pedido.calcularValorTotal(), "ana@email.com"));
        System.out.println(pedido);

        // ===== Testes de integridade (Passo 5 do roteiro da Aula 07) =====
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
            pedidoVazio.pagarCom(new Pix(new BigDecimal("10.00"), "ana@email.com"));
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
    }
}
