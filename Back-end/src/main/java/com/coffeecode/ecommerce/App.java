package com.coffeecode.ecommerce;
import com.coffeecode.ecommerce.model.Produto;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        Produto monitor = new Produto("cod001", "Monitor", "30 polegadas", 2000.00, 20);
        Produto teclado = new Produto("cod002", "Teclado", "RGB", 30.00, 2000);

        System.out.println(teclado.temEstoqueDisponivel(1500));
        System.out.println(monitor.temEstoqueDisponivel(2000));

        teclado.baixarEstoque(200);

        System.out.println(monitor.toString());
        System.out.println(teclado.toString());

    }
}
