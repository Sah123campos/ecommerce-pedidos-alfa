package com.senai.ecommerce.modelo;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Cliente")
class ClienteTest {

    private Endereco endereco;
    private Cliente ana;

    @BeforeEach
    void prepararCenario() {
        endereco = new Endereco("13560-000", "Rua das Flores", "100", "Centro", "São Carlos", "SP");
        ana = new Cliente("Ana Souza", "123.456.789-00", "ana@email.com", endereco);
    }

    @Nested
    @DisplayName("Criação")
    class Criacao {

        @Test
        @DisplayName("Deve criar cliente com os dados informados quando todos são válidos")
        void deveCriarClienteQuandoDadosValidos() {
            assertAll(
                    () -> assertEquals("Ana Souza", ana.getNome()),
                    () -> assertEquals("123.456.789-00", ana.getCpf()),
                    () -> assertEquals("ana@email.com", ana.getEmail()),
                    () -> assertSame(endereco, ana.getEndereco()));
        }

        @Test
        @DisplayName("Deve aceitar CPF só com dígitos quando tem 11 números")
        void deveAceitarCpfSemFormatacao() {
            Cliente cliente = new Cliente("Bruno", "12345678900", "bruno@email.com", endereco);
            assertEquals("12345678900", cliente.getCpf());
        }

        @ParameterizedTest
        @ValueSource(strings = { "", "   " })
        @DisplayName("Deve recusar cliente quando o nome é vazio ou só espaços")
        void deveRecusarClienteComNomeVazio(String nome) {
            IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                    () -> new Cliente(nome, "123.456.789-00", "ana@email.com", endereco));
            assertEquals("Nome do cliente é obrigatório", erro.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = { "ana-email.com", "ana@", "@email.com", "ana@email", "ana @email.com", "" })
        @DisplayName("Deve recusar cliente quando o e-mail não tem formato válido")
        void deveRecusarClienteComEmailInvalido(String email) {
            IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                    () -> new Cliente("Ana", "123.456.789-00", email, endereco));
            assertEquals("E-mail inválido: " + email, erro.getMessage());
        }

        @Test
        @DisplayName("Deve recusar cliente quando o e-mail é nulo")
        void deveRecusarClienteComEmailNulo() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Cliente("Ana", "123.456.789-00", null, endereco));
        }

        @ParameterizedTest
        @ValueSource(strings = { "123", "", "1234567890", "123.456.789-0", "abc.def.ghi-jk" })
        @DisplayName("Deve recusar cliente quando o CPF não tem 11 dígitos")
        void deveRecusarClienteComCpfInvalido(String cpf) {
            IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                    () -> new Cliente("Ana", cpf, "ana@email.com", endereco));
            assertEquals("CPF inválido: " + cpf, erro.getMessage());
        }

        @Test
        @DisplayName("Deve recusar cliente quando o CPF é nulo")
        void deveRecusarClienteComCpfNulo() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Cliente("Ana", null, "ana@email.com", endereco));
        }
    }

    @Nested
    @DisplayName("Alteração de dados")
    class Alteracao {

        @Test
        @DisplayName("Deve atualizar o e-mail quando o novo valor é válido")
        void deveAtualizarEmailQuandoNovoValorValido() {
            ana.setEmail("ana.souza@empresa.com.br");
            assertEquals("ana.souza@empresa.com.br", ana.getEmail());
        }

        @Test
        @DisplayName("Deve manter o e-mail anterior quando o novo valor é inválido")
        void deveManterEmailQuandoNovoValorInvalido() {
            assertThrows(IllegalArgumentException.class, () -> ana.setEmail("sem-arroba"));
            assertEquals("ana@email.com", ana.getEmail());
        }

        @Test
        @DisplayName("Deve manter o CPF anterior quando o novo valor é inválido")
        void deveManterCpfQuandoNovoValorInvalido() {
            assertThrows(IllegalArgumentException.class, () -> ana.setCpf("123"));
            assertEquals("123.456.789-00", ana.getCpf());
        }

        @Test
        @DisplayName("Deve manter o nome anterior quando o novo nome é vazio")
        void deveManterNomeQuandoNovoNomeVazio() {
            assertThrows(IllegalArgumentException.class, () -> ana.setNome(""));
            assertEquals("Ana Souza", ana.getNome());
        }
    }
}
