# Plano de Testes — Aula 10

> Entrega da **Atividade Desafiadora da Aula 10** — suíte de testes unitários do domínio

## Identificação

| Campo | Valor |
|---|---|
| Equipe / Squad | _(preencher)_ |
| Branch | `feature/testes-unitarios` |
| Nº do Pull Request | _(preencher após abrir o PR)_ |
| Comando de execução | `mvn test` |

---

## Parte 1 — O que vale a pena testar? (decisão da equipe)

Critério adotado: **teste onde há decisão** — se o método calcula, valida, muda estado ou escolhe um caminho, ele tem teste. Se só devolve um atributo ou monta uma frase, não.

| Elemento do sistema | Testar? | Argumento |
|---|---|---|
| Cálculo do total do pedido | **Sim** | Calcula dinheiro; erro aqui aparece na fatura do cliente. Testado com 1 e 2 itens e com preço alterado depois da inclusão. |
| Getters e setters sem regra | Não | Só devolvem/atribuem; o custo de manutenção supera o retorno. Os que têm validação (`setPreco`, `setEmail`…) **são** testados. |
| Validações dos construtores | **Sim** | É onde mora a regra de entrada; cobertas com `@ParameterizedTest` e valores de fronteira. |
| Método que só imprime no console (`Aplicacao.main`) | Não | Não decide nada; o `AplicacaoTest` do arquétipo (`assertTrue(true)`) foi removido por não provar nada. |
| Mudança de situação do pedido | **Sim** | Regra central: `ABERTO → PAGO` só com pagamento aprovado; recusa mantém `ABERTO`. |
| `toString` das entidades | Não | Só monta frase. |

---

## Parte 2 — Mapa de Exceções (Aula 09) → testes

| Linha do mapa | Teste | Classe |
|---|---|---|
| Produto: nome vazio | `deveRecusarProdutoComNomeVazio` | `ProdutoTest` |
| Produto: preço negativo / zero | `deveRecusarProdutoComPrecoNaoPositivo` | `ProdutoTest` |
| Produto: estoque negativo | `deveRecusarProdutoComEstoqueNegativo` | `ProdutoTest` |
| Produto: código vazio | `deveRecusarProdutoComCodigoVazio` | `ProdutoTest` |
| Cliente: nome vazio | `deveRecusarClienteComNomeVazio` | `ClienteTest` |
| Cliente: e-mail sem formato válido | `deveRecusarClienteComEmailInvalido` | `ClienteTest` |
| Cliente: CPF inválido | `deveRecusarClienteComCpfInvalido` | `ClienteTest` |
| Pedido: quantidade zero ou negativa | `deveRecusarItemComQuantidadeNaoPositiva` | `PedidoTest`, `ItemPedidoTest`, `ProdutoTest` |
| Pedido: estoque insuficiente | `deveLancarExcecaoQuandoEstoqueInsuficiente` | `ProdutoTest`, `PedidoTest` |
| Pedido: produto `null` | `deveRecusarItemComProdutoNulo` | `PedidoTest`, `ItemPedidoTest` |
| Pedido: alterar pedido já pago | `deveRecusarAlterarPedidoJaPago` | `PedidoTest` (`PedidoJaPago`) |
| Pedido: lista de itens protegida | `deveProtegerListaDeItens` | `PedidoTest` |
| Pagamento: processador `null` | `deveRecusarProcessadorNulo` | `PedidoTest` |
| Pagamento: pedido sem itens | `deveRecusarPagamentoDePedidoSemItens` | `PedidoTest` |
| Pagamento: recusado → permanece `ABERTO` | `deveManterPedidoAbertoQuandoPagamentoRecusado` | `PedidoTest` |
| Pagamento: pedido já pago | `deveRecusarPagarPedidoJaPago` (confere que não cobra de novo) | `PedidoTest` (`PedidoJaPago`) |
| Pagamento: cartão com mais de 12 parcelas | `deveRecusarMaisDeDozeParcelas` | `CartaoCreditoTest` |
| Caminho feliz: pedido com 2 itens (R$ 4030,00) | `deveSomarTotalComDoisItens` | `PedidoTest` |
| Caminho feliz: pagar com Pix → `PAGO` | `devePagarPedidoComPixESituacaoFicarPaga` | `PedidoTest` |

---

## Parte 3 — Suíte por classe

| Classe testada | Classe de teste | Métodos de teste | Sucesso (mín. 3) | Falha (`assertThrows`) |
|---|---|---|---|---|
| `Produto` | `ProdutoTest` | 20 | criação, baixa, zerar estoque, reposição, preço, disponibilidade | nome, código, preço, estoque, quantidade, insuficiente |
| `Cliente` | `ClienteTest` | 11 | criação, CPF sem máscara, alterar e-mail | nome, e-mail, CPF |
| `ItemPedido` | `ItemPedidoTest` | 8 | criação, subtotal ×1, subtotal ×N, brinde | produto nulo, quantidade, preço |
| `Pedido` | `PedidoTest` | 19 | criação, item, total 1 e 2 itens, pagar, nova tentativa | cliente, produto, quantidade, estoque, sem itens, já pago, recusado |
| `Pix` | `PixTest` | 9 | criação, aprovação, descrição, setValor | chave, valor |
| `Boleto` | `BoletoTest` | 8 | criação, vencimento hoje, aguardando, descrição | vencimento passado/nulo, valor |
| `CartaoCredito` | `CartaoCreditoTest` | 11 | criação, 12x, parcela mínima, aprovação, estorno | >12x, ≤0x, parcela < R$ 5, número, valor |
| `Dinheiro` | `DinheiroTest` | 6 | troco, valor exato, descrição | valor recebido |
| Fluxo polimórfico | `ProcessadorPagamentoPolimorficoTest` | 5 | todas aprovam/aguardam, descrição, parcelamento, estorno | — |
| `Validador` | `ValidadorTest` | 13 | — | todos os `exigir*` |
| Exceções do domínio | `ExcecoesDoDominioTest` | 5 | hierarquia, mensagens, causa encadeada | — |
| **Total** | | **115 métodos** (156 execuções com os parametrizados) | | |

Requisitos desejáveis atendidos: `@ParameterizedTest`, `@Nested` (`ProdutoTest`, `ClienteTest`, `PedidoTest`), `assertAll`, método auxiliar `criarPedidoComDoisItens` e processador falso (`ProcessadorFalso`) para isolar o `Pedido` das regras de Pix/Boleto/Cartão.

---

## Parte 4 — Cobertura

> Medir pela IDE (IntelliJ: *Run with Coverage*; Eclipse: *Coverage As*) e preencher abaixo. Meta: **≥ 70% de linhas** nas classes de domínio.

| Classe | Cobertura de linhas | Observação |
|---|---|---|
| `Produto` | _(medir)_ | |
| `Cliente` | _(medir)_ | |
| `ItemPedido` | _(medir)_ | |
| `Pedido` | _(medir)_ | |
| `Pix` / `Boleto` / `CartaoCredito` / `Dinheiro` | _(medir)_ | |
| `FormaPagamento` | _(medir)_ | |
| `Validador` | _(medir)_ | |

---

## Parte 5 — Prova de que os testes pegam defeito (mutação manual)

Cada alteração abaixo foi feita **no código de produção** (e desfeita em seguida); a suíte precisou ficar vermelha.

| Defeito injetado | Resultado |
|---|---|
| `baixarEstoque`: `>` virou `>=` | vermelho (1 falha) |
| `baixarEstoque`: sem validar quantidade positiva | vermelho (4 falhas) |
| `baixarEstoque`: não desconta o estoque | vermelho (4 falhas) |
| `Pedido.pagar`: não checa pedido já pago | vermelho (2 falhas) |
| `Pedido.pagar`: não checa pedido sem itens | vermelho (1 falha) |
| `Pedido.pagar`: vira `PAGO` mesmo com pagamento recusado | vermelho (2 falhas) |
| `Pedido.adicionarItem`: não bloqueia pedido pago | vermelho (2 falhas) |
| `ItemPedido`: subtotal ignora a quantidade | vermelho (6 falhas) |
| `CartaoCredito`: limite de 12 vira 24 parcelas | vermelho (3 falhas) |
| `Boleto.processar` passa a aprovar | vermelho (2 falhas) |
| `Dinheiro`: `<` vira `<=` (valor exato) | vermelho (1 falha) |
| `Validador`: e-mail aceita qualquer texto | vermelho (12 falhas) |

---

## Parte 6 — Achados durante a escrita dos testes (fora do escopo desta aula)

- `Cliente.toString()` lança `NullPointerException` se o endereço for `null` (o construtor aceita `null`).
- `Pedido.pagar` não confere se o valor do processador bate com o total do pedido (um `Pix` de R$ 10,00 paga um pedido de R$ 4030,00). O roteiro da Aula 09 listava "valor divergente do total", mas o mapa da equipe não.
- `Cliente` não tem `equals`/`hashCode`, então "comparação por documento" (sugerida no roteiro) não existe para testar.
- `Pedido` não valida o `numero` (aceita `null`).

> Nenhum desses itens foi alterado no código — ficam como decisão da equipe.
