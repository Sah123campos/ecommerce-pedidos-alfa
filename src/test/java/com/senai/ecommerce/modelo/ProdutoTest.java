package com.senai.ecommerce.modelo;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.senai.ecommerce.excecao.EstoqueInsuficienteException;

@DisplayName("Produto")
class ProdutoTest {

    private Produto notebook;

    @BeforeEach
    void prepararCenario() {
        notebook = new Produto("NB-001", "Notebook", 3000.00, 5);
    }

    @Nested
    @DisplayName("Criação")
    class Criacao {

        @Test
        @DisplayName("Deve criar produto ativo com os dados informados quando todos são válidos")
        void deveCriarProdutoAtivoQuandoDadosValidos() {
            assertAll(
                    () -> assertEquals("NB-001", notebook.getCodigo()),
                    () -> assertEquals("Notebook", notebook.getNome()),
                    () -> assertEquals(3000.00, notebook.getPreco(), 0.0001),
                    () -> assertEquals(5, notebook.getQuantidadeEmEstoque()),
                    () -> assertTrue(notebook.isAtivo()));
        }

        @Test
        @DisplayName("Deve aceitar estoque zero quando o produto ainda não foi reposto")
        void deveAceitarEstoqueZero() {
            Produto semEstoque = new Produto("NB-002", "Notebook Gamer", 5000.00, 0);
            assertEquals(0, semEstoque.getQuantidadeEmEstoque());
        }

        @ParameterizedTest
        @ValueSource(strings = { "", "   " })
        @DisplayName("Deve recusar produto quando o nome é vazio ou só espaços")
        void deveRecusarProdutoComNomeVazio(String nome) {
            IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                    () -> new Produto("NB-003", nome, 10.00, 5));
            assertEquals("Nome do produto é obrigatório", erro.getMessage());
        }

        @Test
        @DisplayName("Deve recusar produto quando o nome é nulo")
        void deveRecusarProdutoComNomeNulo() {
            assertThrows(IllegalArgumentException.class, () -> new Produto("NB-003", null, 10.00, 5));
        }

        @ParameterizedTest
        @ValueSource(strings = { "", "  " })
        @DisplayName("Deve recusar produto quando o código é vazio")
        void deveRecusarProdutoComCodigoVazio(String codigo) {
            IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                    () -> new Produto(codigo, "Mouse", 10.00, 5));
            assertEquals("Código é obrigatório", erro.getMessage());
        }

        @ParameterizedTest
        @ValueSource(doubles = { 0.0, -10.0, -0.01 })
        @DisplayName("Deve recusar produto quando o preço é zero ou negativo")
        void deveRecusarProdutoComPrecoNaoPositivo(double preco) {
            IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                    () -> new Produto("MS-001", "Mouse", preco, 5));
            assertTrue(erro.getMessage().startsWith("Preço deve ser maior que zero"));
        }

        @ParameterizedTest
        @ValueSource(ints = { -1, -3 })
        @DisplayName("Deve recusar produto quando o estoque inicial é negativo")
        void deveRecusarProdutoComEstoqueNegativo(int estoque) {
            IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                    () -> new Produto("MS-001", "Mouse", 50.00, estoque));
            assertTrue(erro.getMessage().startsWith("Estoque não pode ser negativo"));
        }
    }

    @Nested
    @DisplayName("Baixa de estoque")
    class BaixaDeEstoque {

        @Test
        @DisplayName("Deve baixar o estoque quando há quantidade suficiente")
        void deveBaixarEstoqueQuandoHaQuantidadeSuficiente() throws Exception {
            notebook.baixarEstoque(2);
            assertEquals(3, notebook.getQuantidadeEmEstoque());
        }

        @Test
        @DisplayName("Deve zerar o estoque quando a quantidade pedida é exatamente a disponível")
        void deveZerarEstoqueQuandoQuantidadeIgualAoDisponivel() throws Exception {
            notebook.baixarEstoque(5);
            assertEquals(0, notebook.getQuantidadeEmEstoque());
        }

        @Test
        @DisplayName("Deve lançar exceção com os dados do problema quando o estoque é insuficiente")
        void deveLancarExcecaoQuandoEstoqueInsuficiente() {
            EstoqueInsuficienteException erro = assertThrows(EstoqueInsuficienteException.class,
                    () -> notebook.baixarEstoque(50));

            assertAll(
                    () -> assertEquals(50, erro.getQuantidadeSolicitada()),
                    () -> assertSame(notebook, erro.getProduto()),
                    () -> assertTrue(erro.getMessage().contains("Notebook")),
                    () -> assertTrue(erro.getMessage().contains("disponível 5")),
                    // a baixa não pode deixar o objeto num estado intermediário
                    () -> assertEquals(5, notebook.getQuantidadeEmEstoque()));
        }

        @Test
        @DisplayName("Deve lançar exceção quando se pede apenas uma unidade além do estoque")
        void deveLancarExcecaoQuandoPedeUmaUnidadeAlemDoEstoque() {
            assertThrows(EstoqueInsuficienteException.class, () -> notebook.baixarEstoque(6));
            assertEquals(5, notebook.getQuantidadeEmEstoque());
        }

        @ParameterizedTest
        @ValueSource(ints = { 0, -1, -50 })
        @DisplayName("Deve recusar quantidade não positiva sem alterar o estoque")
        void deveRecusarQuantidadeNaoPositiva(int quantidade) {
            assertThrows(IllegalArgumentException.class, () -> notebook.baixarEstoque(quantidade));
            assertEquals(5, notebook.getQuantidadeEmEstoque());
        }
    }

    @Nested
    @DisplayName("Disponibilidade")
    class Disponibilidade {

        @Test
        @DisplayName("Deve informar disponível quando a quantidade desejada cabe no estoque")
        void deveInformarDisponivelQuandoQuantidadeCabeNoEstoque() {
            assertTrue(notebook.temEstoqueDisponivel(5));
        }

        @Test
        @DisplayName("Deve informar indisponível quando a quantidade desejada excede o estoque")
        void deveInformarIndisponivelQuandoQuantidadeExcedeEstoque() {
            assertFalse(notebook.temEstoqueDisponivel(6));
        }

        @Test
        @DisplayName("Deve informar indisponível quando o produto está inativo, mesmo com estoque")
        void deveInformarIndisponivelQuandoProdutoInativo() {
            notebook.setAtivo(false);
            assertFalse(notebook.temEstoqueDisponivel(1));
        }
    }

    @Nested
    @DisplayName("Reposição e alteração de dados")
    class ReposicaoEAlteracao {

        @Test
        @DisplayName("Deve atualizar o estoque quando a reposição é válida")
        void deveAtualizarEstoqueQuandoReposicaoValida() {
            notebook.setQuantidadeEmEstoque(20);
            assertEquals(20, notebook.getQuantidadeEmEstoque());
        }

        @Test
        @DisplayName("Deve manter o estoque anterior quando a reposição é negativa")
        void deveManterEstoqueQuandoReposicaoNegativa() {
            assertThrows(IllegalArgumentException.class, () -> notebook.setQuantidadeEmEstoque(-1));
            assertEquals(5, notebook.getQuantidadeEmEstoque());
        }

        @Test
        @DisplayName("Deve atualizar o preço quando o novo valor é positivo")
        void deveAtualizarPrecoQuandoValorPositivo() {
            notebook.setPreco(2800.00);
            assertEquals(2800.00, notebook.getPreco(), 0.0001);
        }

        @Test
        @DisplayName("Deve manter o preço anterior quando o novo valor é inválido")
        void deveManterPrecoQuandoNovoValorInvalido() {
            assertThrows(IllegalArgumentException.class, () -> notebook.setPreco(0));
            assertEquals(3000.00, notebook.getPreco(), 0.0001);
        }

        @Test
        @DisplayName("Deve manter o nome anterior quando o novo nome é vazio")
        void deveManterNomeQuandoNovoNomeVazio() {
            assertThrows(IllegalArgumentException.class, () -> notebook.setNome(" "));
            assertEquals("Notebook", notebook.getNome());
        }
    }
}
