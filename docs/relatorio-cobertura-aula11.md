# Relatório Técnico de Cobertura — Aula 11

> Entrega da **Atividade Desafiadora da Aula 11** — testes de integração, Mockito e JaCoCo

## Identificação

| Campo | Valor |
|---|---|
| Equipe / Squad | _(preencher)_ |
| Branch | `feature/testes-integracao` |
| Nº do Pull Request | _(preencher após abrir o PR)_ |
| Comando | `mvn clean test` → relatório em `target/site/jacoco/index.html` |
| Cópia do relatório (HTML) | `docs/cobertura-jacoco-aula11/index.html` |

> Os números abaixo foram medidos com JaCoCo 0.8.11 sobre os 171 testes (156 da Aula 10 + 15 de integração), todos verdes. A classe `Aplicacao` (ponto de entrada que só imprime no console) está **excluída** da medição no `pom.xml`, pelo mesmo critério do plano de testes da Aula 10: não decide nada.

---

## Parte 1 — Testes de integração entregues

Classe: `FluxoDePedidoIntegracaoTest` (pacote `com.senai.ecommerce.integracao`). Todas as classes do domínio são **de verdade**; o único dublê é o gateway de pagamento (Mockito).

| Grupo | Teste | O que prova no encaixe |
|---|---|---|
| Caminho feliz | fluxo completo com Pix | cadastrar → pedido → item → pagar → estoque baixa, situação `PAGO`, comprovante gerado |
| Caminho feliz | pagamento em dinheiro com troco | o troco é calculado sobre o total que o `Pedido` calculou |
| Caminho feliz | pagamento com cartão | `PAGO` e forma de pagamento guardada |
| Total cobrado | gateway recebe o total calculado | `verify(gateway).processar(total)` |
| Total cobrado | gateway recusa | `PagamentoRecusadoException`, pedido `ABERTO`, forma não registrada |
| Total cobrado | pedido já pago | gateway **não** é cobrado de novo (`times(1)`) |
| Total cobrado | pedido sem itens | gateway nunca é acionado (`never()`) |
| Estoque | estoque insuficiente | item não entra e estoque não muda |
| Estoque | dois pedidos no mesmo produto | o segundo recebe só o que sobrou |
| Estoque | pagamento recusado | **ponto frágil** (ver Parte 4) |
| Estoque | pedido pago | não aceita item novo; estoque não baixa |
| Cadastro | produto/cliente inválidos | objeto inválido nunca nasce, então nunca entra em pedido |
| Cadastro | produto reabastecido | volta a ser vendável |
| Polimorfismo | um laço, quatro formas | Pix, cartão e dinheiro aprovam; boleto fica pendente |

**Dublê usado:** `mock(ProcessadorPagamento.class)` — *mock/stub* do gateway. `when(...).thenReturn(...)` programa a resposta; `verify(...)` confere a interação. Escolha justificada: cobrar um gateway real a cada `mvn test` seria lento, caro e frágil.

---

## Parte 2 — Cobertura por pacote

| Pacote | Instruções | Ramos | Linhas |
|---|---|---|---|
| `com/senai/ecommerce/modelo/pagamento` | 100% (244/244) | 100% (10/10) | 100% (66/66) |
| `com/senai/ecommerce/excecao` | 96% (76/79) | n/a | 96% (26/27) |
| `com/senai/ecommerce/modelo` | 72% (367/507) | 89% (16/18) | 77% (114/148) |
| `com/senai/ecommerce/util` | 100% (99/99) | 100% (26/26) | 100% (28/28) |
| **Total** | **85% (786/929)** | **96% (52/54)** | **87% (234/269)** |

**Meta da equipe: 70% → atingida (85% de instruções).**

Sem a exclusão de `Aplicacao`, o total de instruções seria 49% (786/1607) — a classe tem 678 instruções e nenhuma é exercitada. Registramos o número honesto para não parecer que a exclusão é um truque.

---

## Parte 3 — Classes abaixo da meta

| Classe | Instruções | Por quê |
|---|---|---|
| `modelo/Endereco` | 27% | Só getters, setters e `toString`; pelo plano da Aula 10 não testamos código sem decisão. |
| `modelo/Cliente` | 68% | As validações (nome, e-mail, CPF) estão cobertas; o que falta é `telefone`, `endereco` e `toString`. |
| `modelo/Produto` | 73% | Validações e `baixarEstoque` cobertos; falta código sem regra (getters/`toString`). |
| `modelo/Pedido` | 84% | Acima da meta; 2 ramos descobertos de 12. |

A pergunta diante de cada vermelho é "esta regra importa?". Nestes casos, não: nenhuma decisão de negócio está sem teste. O vermelho que importaria seria em `Pedido.pagar` ou `Produto.baixarEstoque`, e ambos estão verdes.

---

## Parte 4 — Pontos frágeis encontrados (para a discussão do Momento 4)

1. **Estoque baixa ao adicionar o item, não ao pagar.** Se o pagamento é recusado, o estoque continua reservado e o pedido fica `ABERTO` (teste `pagamentoRecusadoMantemEstoqueReservado`). Hoje não há cancelamento de pedido nem expiração de reserva. Isso é uma decisão de desenho a registrar, não um bug do teste.
2. **Boleto sempre vira `PagamentoRecusadoException`.** `Boleto.processar` devolve `false` ("aguardando compensação") e `Pedido.pagar` trata `false` como recusa. Semanticamente "pendente" não é "recusado".
3. **A integração não aumentou a cobertura.** Rodando só a suíte da Aula 10, o total já era 85%. O valor dos 15 testes novos não está em linhas a mais, e sim em provar o **encaixe**: o total cobrado, a baixa de estoque e a situação do pedido juntos. É exatamente o que a cobertura não mede.

---

## Parte 5 — O que a cobertura não mede

Cobertura mostra o que foi executado, não se a asserção verifica a coisa certa. Por isso os testes de integração conferem valores de negócio (total, estoque, situação, quantidade de cobranças) e não apenas "não lançou exceção".

---

## Parte 6 — Fica para a Aula 12

- [ ] `Produto` e `Cliente` passam a ser persistidos: o fluxo de integração ganha um repositório real (ou *fake* em memória).
- [ ] Decidir onde o estoque deve baixar (ao adicionar, ao pagar ou com reserva expirável).
- [ ] Separar "pagamento pendente" de "pagamento recusado" (ponto frágil 2).
