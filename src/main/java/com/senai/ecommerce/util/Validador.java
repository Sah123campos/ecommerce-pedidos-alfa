package com.senai.ecommerce.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Validações genéricas reaproveitadas por todo o domínio (Produto,
 * Cliente, Pedido, formas de pagamento...). Cada método lança
 * {@link IllegalArgumentException} — a mesma exceção da linguagem que
 * já expressa bem "erro de uso da classe" — para que ninguém precise
 * reinventar o mesmo {@code if} em dez lugares diferentes.
 *
 * Validações específicas de uma regra de negócio (limite de parcelas
 * do cartão, vencimento do boleto) continuam onde a regra mora.
 */
public final class Validador {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private Validador() {
    }

    public static void exigirNaoNulo(Object valor, String mensagem) {
        if (valor == null) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    public static void exigirNaoVazio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    public static void exigirPositivo(int valor, String mensagem) {
        if (valor <= 0) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    public static void exigirPositivo(double valor, String mensagem) {
        if (valor <= 0) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    public static void exigirPositivo(BigDecimal valor, String mensagem) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    public static void exigirNaoNegativo(int valor, String mensagem) {
        if (valor < 0) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    public static void exigirNaoNegativo(double valor, String mensagem) {
        if (valor < 0) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    public static void exigirEmailValido(String email, String mensagem) {
        if (email == null || !EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    public static void exigirCpfValido(String cpf, String mensagem) {
        if (cpf == null || cpf.replaceAll("\\D", "").length() != 11) {
            throw new IllegalArgumentException(mensagem);
        }
    }
}
