package com.senai.ecommerce.excecao;

/**
 * Raiz comum de todas as exceções de negócio do domínio.
 *
 * Ninguém lança {@code ECommerceException} diretamente — ela existe para
 * que a camada mais externa (hoje, {@code Aplicacao}; amanhã, um
 * Controller REST) possa capturar tudo o que é falha de negócio com um
 * único catch, sem capturar o que não é (bugs de programação, que
 * continuam sendo {@link RuntimeException}).
 *
 * É checked de propósito: toda falha de negócio prevista precisa
 * aparecer na assinatura do método e ser considerada por quem chama.
 */
public class ECommerceException extends Exception {

    public ECommerceException(String mensagem) {
        super(mensagem);
    }

    public ECommerceException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
