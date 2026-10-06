package com.senai.ecommerce.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Validador")
class ValidadorTest {

    @Test
    @DisplayName("Deve lançar IllegalArgumentException com a mensagem informada quando o objeto é nulo")
    void deveRecusarNulo() {
        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> Validador.exigirNaoNulo(null, "obrigatório"));
        assertEquals("obrigatório", erro.getMessage());
    }

    @Test
    @DisplayName("Deve aceitar objeto não nulo")
    void deveAceitarNaoNulo() {
        assertDoesNotThrow(() -> Validador.exigirNaoNulo("x", "obrigatório"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "\t" })
    @DisplayName("Deve recusar texto vazio ou só com espaços")
    void deveRecusarTextoVazio(String texto) {
        assertThrows(IllegalArgumentException.class, () -> Validador.exigirNaoVazio(texto, "msg"));
    }

    @Test
    @DisplayName("Deve recusar texto nulo e aceitar texto preenchido")
    void deveTratarTextoNuloEPreenchido() {
        assertThrows(IllegalArgumentException.class, () -> Validador.exigirNaoVazio(null, "msg"));
        assertDoesNotThrow(() -> Validador.exigirNaoVazio("a", "msg"));
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, -1 })
    @DisplayName("Deve recusar inteiro que não é positivo")
    void deveRecusarInteiroNaoPositivo(int valor) {
        assertThrows(IllegalArgumentException.class, () -> Validador.exigirPositivo(valor, "msg"));
    }

    @Test
    @DisplayName("Deve aceitar o menor inteiro positivo")
    void deveAceitarInteiroUm() {
        assertDoesNotThrow(() -> Validador.exigirPositivo(1, "msg"));
    }

    @ParameterizedTest
    @ValueSource(doubles = { 0.0, -0.01 })
    @DisplayName("Deve recusar double que não é positivo")
    void deveRecusarDoubleNaoPositivo(double valor) {
        assertThrows(IllegalArgumentException.class, () -> Validador.exigirPositivo(valor, "msg"));
    }

    @Test
    @DisplayName("Deve recusar BigDecimal nulo, zero ou negativo e aceitar positivo")
    void deveValidarBigDecimalPositivo() {
        assertThrows(IllegalArgumentException.class, () -> Validador.exigirPositivo((BigDecimal) null, "msg"));
        assertThrows(IllegalArgumentException.class, () -> Validador.exigirPositivo(BigDecimal.ZERO, "msg"));
        assertThrows(IllegalArgumentException.class, () -> Validador.exigirPositivo(new BigDecimal("-1"), "msg"));
        assertDoesNotThrow(() -> Validador.exigirPositivo(new BigDecimal("0.01"), "msg"));
    }

    @Test
    @DisplayName("Deve aceitar zero e recusar negativo nas validações de não negativo")
    void deveValidarNaoNegativo() {
        assertDoesNotThrow(() -> Validador.exigirNaoNegativo(0, "msg"));
        assertDoesNotThrow(() -> Validador.exigirNaoNegativo(0.0, "msg"));
        assertThrows(IllegalArgumentException.class, () -> Validador.exigirNaoNegativo(-1, "msg"));
        assertThrows(IllegalArgumentException.class, () -> Validador.exigirNaoNegativo(-0.01, "msg"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "ana@email.com", "a.b+c@dominio.com.br" })
    @DisplayName("Deve aceitar e-mail com formato válido")
    void deveAceitarEmailValido(String email) {
        assertDoesNotThrow(() -> Validador.exigirEmailValido(email, "msg"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "ana", "ana@", "@email.com", "ana@email", "a na@email.com" })
    @DisplayName("Deve recusar e-mail com formato inválido")
    void deveRecusarEmailInvalido(String email) {
        assertThrows(IllegalArgumentException.class, () -> Validador.exigirEmailValido(email, "msg"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "123.456.789-00", "12345678900" })
    @DisplayName("Deve aceitar CPF com 11 dígitos, formatado ou não")
    void deveAceitarCpfValido(String cpf) {
        assertDoesNotThrow(() -> Validador.exigirCpfValido(cpf, "msg"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "123", "1234567890", "123456789012" })
    @DisplayName("Deve recusar CPF que não tem 11 dígitos")
    void deveRecusarCpfInvalido(String cpf) {
        assertThrows(IllegalArgumentException.class, () -> Validador.exigirCpfValido(cpf, "msg"));
    }
}
