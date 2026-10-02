# Mapa de Exceções do Sistema

> Entrega da **Aula 09** da Atividade Desafiadora

## Identificação

| Campo | Valor |
|---|---|
| Equipe / Squad | _(preencher)_ |
| Branch | `feature/tratamento-excecoes` |
| Nº do Pull Request | _(preencher após abrir o PR)_ |
| Pacote de exceções | `com.senai.ecommerce.excecao` |

---

## Parte 1 — O critério da equipe

| Pergunta | Resposta da equipe |
|---|---|
| Quando uma exceção será **checked**? | Quando é uma situação de negócio prevista e quem chamou o método tem como reagir de forma útil (oferecer outro produto, sugerir outra forma de pagamento, não tentar cobrar de novo). |
| Quando será **unchecked**? | Quando é defeito de quem chamou — argumento nulo, vazio ou negativo, estado que só se resolve corrigindo o código que fez a chamada. Usamos as exceções da própria linguagem (`IllegalArgumentException`) para isso. |
| Qual é a exceção-raiz do domínio? | `ECommerceException` (checked, estende `Exception`). |
| Quem trata: o domínio ou a camada de apresentação? | O domínio (`Produto`, `Pedido`) só detecta e lança — ele não sabe o que o usuário quer. Quem trata é a camada de apresentação (`Aplicacao`), que tem contexto para decidir a mensagem e o próximo passo. |
| O que fazemos quando não sabemos tratar? | Deixamos subir. Nenhum `catch` existe só para não deixar o método declarar `throws`. |

---

## Parte 2 — Hierarquia de exceções do projeto

| Exceção | Herda de | Checked? | Dados que carrega | Quem lança |
|---|---|---|---|---|
| `ECommerceException` | `Exception` | sim | mensagem, causa | _(raiz — ninguém lança diretamente)_ |
| `EstoqueInsuficienteException` | `ECommerceException` | sim | `produto`, `quantidadeSolicitada` | `Produto.baixarEstoque` |
| `PagamentoRecusadoException` | `ECommerceException` | sim | `formaPagamento`, `motivo` | `Pedido.pagar` |
| `PedidoInvalidoException` | `ECommerceException` | sim | `numeroPedido`, `motivo` | `Pedido.adicionarItem`, `Pedido.pagar` |

**Exceções da linguagem que decidimos reaproveitar (em vez de criar as nossas):**

| Exceção | Onde usamos | Por quê |
|---|---|---|
| `IllegalArgumentException` | Construtores e setters de `Produto`, `Cliente`, `ItemPedido`, `Pedido` e das formas de pagamento (nome/chave/CPF/e-mail vazio, preço/quantidade/valor ≤ 0, cartão com mais de 12 parcelas) | É defeito de quem chamou — corrigir o código é a única reação útil. Criar uma exceção customizada só mudaria o nome. |
| `UnsupportedOperationException` | `Pedido.getItens()` (via `Collections.unmodifiableList`) | Já expressa exatamente "esta lista não pode ser alterada por fora"; é da própria coleção, não precisa de nome de negócio. |

> `IllegalStateException` não é mais usada no fluxo de pedido: pedido sem itens, pedido já pago e alteração de pedido pago migraram para `PedidoInvalidoException`, porque são situações de negócio recuperáveis (quem chama pode reagir), não apenas "estado interno inválido".

---

## Parte 3 — Mapa de validações por fluxo

### Cadastro de produto

| # | Entrada / situação | Exceção esperada | Mensagem exibida | Obtido | OK |
|---|---|---|---|---|---|
| 1 | Nome vazio | `IllegalArgumentException` | "Nome do produto é obrigatório" | igual ao esperado | ✅ |
| 2 | Preço negativo | `IllegalArgumentException` | "Preço deve ser maior que zero: -10.0" | igual ao esperado | ✅ |
| 3 | Preço zero | `IllegalArgumentException` | "Preço deve ser maior que zero: 0.0" | igual ao esperado | ✅ |
| 4 | Estoque negativo | `IllegalArgumentException` | "Estoque não pode ser negativo: -3" | igual ao esperado | ✅ |
| 5 | Código vazio | `IllegalArgumentException` | "Código é obrigatório" | igual ao esperado | ✅ |

### Cadastro de cliente

| # | Entrada / situação | Exceção esperada | Mensagem exibida | Obtido | OK |
|---|---|---|---|---|---|
| 1 | Nome vazio | `IllegalArgumentException` | "Nome do cliente é obrigatório" | igual ao esperado | ✅ |
| 2 | E-mail sem `@` | `IllegalArgumentException` | "E-mail inválido: ana-email.com" | igual ao esperado | ✅ |
| 3 | Documento (CPF) inválido | `IllegalArgumentException` | "CPF inválido: 123" | igual ao esperado | ✅ |

### Pedido

| # | Entrada / situação | Exceção esperada | Mensagem exibida | Obtido | OK |
|---|---|---|---|---|---|
| 1 | Quantidade zero ou negativa | `IllegalArgumentException` | "Quantidade deve ser positiva" | igual ao esperado | ✅ |
| 2 | Produto com estoque insuficiente | `EstoqueInsuficienteException` | "Estoque insuficiente de Monitor: disponível 18, solicitado 9999" | igual ao esperado | ✅ |
| 3 | Produto `null` | `IllegalArgumentException` | "Produto é obrigatório" | igual ao esperado | ✅ |
| 4 | Adicionar item a pedido já pago | `PedidoInvalidoException` | "Pedido PED-0002: não é possível alterar um pedido já pago" | igual ao esperado | ✅ |
| 5 | Alterar lista de itens por fora (`getItens().clear()`) | `UnsupportedOperationException` | — | igual ao esperado | ✅ |

### Pagamento

| # | Entrada / situação | Exceção esperada | Mensagem exibida | Obtido | OK |
|---|---|---|---|---|---|
| 1 | Processador `null` | `IllegalArgumentException` | "Forma de pagamento é obrigatória" | igual ao esperado | ✅ |
| 2 | Pedido sem itens | `PedidoInvalidoException` | "Pedido PED-0003: pedido sem itens não pode ser pago" | igual ao esperado | ✅ |
| 3 | Pagamento recusado (dinheiro insuficiente) | `PagamentoRecusadoException` | "Pagamento por Dinheiro (recebido R$ 5.00) não aprovado: pagamento não aprovado no momento" | igual ao esperado | ✅ |
| 4 | Pedido já pago | `PedidoInvalidoException` | "Pedido PED-0001: pedido já está pago" | igual ao esperado | ✅ |
| 5 | Cartão com mais de 12 parcelas | `IllegalArgumentException` | "Cartão não aceita mais de 12 parcelas" | igual ao esperado | ✅ |

### Caminho feliz (não esqueçam!)

| # | Cenário | Esperado | Obtido | OK |
|---|---|---|---|---|
| 1 | Cadastrar produto válido | criado sem exceção | criado sem exceção | ✅ |
| 2 | Criar pedido com 2 itens | total correto (R$ 4030,00: 2 monitores + 1 teclado) | total correto | ✅ |
| 3 | Pagar com Pix | situação `PAGO` | situação `PAGO` | ✅ |

> Rodamos o caminho feliz **depois** de todas as validações — e ele continuou passando. Se tivesse quebrado, o defeito estaria na validação, não no cenário.

---

## Parte 4 — Onde cada exceção é tratada

| Exceção | Lançada em | Tratada em | O que o tratamento faz |
|---|---|---|---|
| `EstoqueInsuficienteException` | `Produto.baixarEstoque` (chamado por `Pedido.adicionarItem`) | `Aplicacao` (camada de apresentação) | Informa a quantidade disponível agora, para o cliente ajustar o pedido |
| `PagamentoRecusadoException` | `Pedido.pagar` | `Aplicacao` | Informa o motivo da recusa; a situação do pedido permanece `ABERTO`, para o cliente tentar outra forma |
| `PedidoInvalidoException` | `Pedido.adicionarItem`, `Pedido.pagar` | `Aplicacao` | Informa a situação atual do pedido e por que a operação não é permitida agora |

---

## Parte 5 — Caça aos antipadrões

| Antipadrão | Quantas ocorrências? | Como foi corrigido |
|---|---|---|
| `catch` vazio | 0 | Nenhum encontrado — todo `catch` do projeto imprime uma mensagem ou traduz a exceção |
| `e.printStackTrace()` no código entregue | 0 | Nenhum encontrado |
| `catch (Exception e)` genérico | 0 | O único catch "amplo" é `catch (ECommerceException e)`, usado de propósito como rede de segurança da camada de apresentação — não é um `catch (Exception e)` indiscriminado |
| Exceção usada para controlar fluxo normal | 0 | Pagamento recusado é evento excepcional (o esperado é aprovar); não usamos exceção para decisões rotineiras |
| Mensagem genérica (`"erro"`, `"falhou"`) | 0 | Toda mensagem cita o dado concreto (produto, quantidade, forma de pagamento, número do pedido) |
| Recurso aberto e não fechado | 0 | O único recurso (`Scanner`, na demonstração de encadeamento) usa try-with-resources |

---

## Parte 7 — Fica para a Aula 10

- [x] `deveLancarExcecaoQuandoEstoqueInsuficiente()`
- [x] `deveLancarExcecaoQuandoPrecoNegativo()`
- [x] `deveLancarExcecaoQuandoPrecoZero()`
- [x] `deveLancarExcecaoQuandoPedidoSemItens()`
- [x] `deveLancarExcecaoQuandoProcessadorNulo()`
- [x] `deveLancarExcecaoQuandoPedidoJaPago()`
- [x] `deveLancarExcecaoQuandoAlterarPedidoJaPago()`
- [x] `deveManterPedidoAbertoQuandoPagamentoRecusado()`
- [x] `deveLancarExcecaoQuandoCartaoComMaisDeDozeParcelas()`
- [x] `deveLancarExcecaoQuandoEmailInvalido()`
- [x] `deveLancarExcecaoQuandoCpfInvalido()`
- [x] `deveCalcularTotalCorretamenteComDoisItens()` _(caminho feliz)_
- [x] `devePagarPedidoComPixESituacaoFicarPaga()` _(caminho feliz)_

> Cada linha acima corresponde a um bloco `try/catch` já executado manualmente em
> `Aplicacao.main()` nesta aula — a Aula 10 só precisa trocar `try/catch` e
> `System.out.println` por `assertThrows` e `assertEquals`.
