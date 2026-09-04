package com.ecommerce.pedidos.alfa;

import com.ecommerce.pedidos.alfa.model.Produto;

/**
 * Hello world!
 */
public class App {
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
    }
}
