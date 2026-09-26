# Registro de Testes Funcionais — Módulo de Pagamento Polimórfico (Aula 08)

Todos os cenários abaixo estão implementados e executados em
`Aplicacao.main()`. Rodar com `mvn compile exec:java -Dexec.mainClass="com.senai.ecommerce.Aplicacao"`.

| Cenário | Resultado esperado | Resultado obtido |
|---|---|---|
| Pagar pedido com Pix | Aprovado; situação vira PAGO; comprovante gerado | ✅ Aprovado, comprovante `PIX-...` |
| Pagar pedido com Boleto | Não aprovado ainda; situação permanece ABERTO | ✅ `processar` devolve `false`, situação continua ABERTO |
| Pagar com cartão em 3 parcelas | Aprovado; comprovante com a bandeira | ✅ Aprovado, comprovante `CARTAO-...` |
| Pagar com cartão em 15 parcelas | `IllegalArgumentException` | ✅ "Cartão não aceita mais de 12 parcelas" |
| Pagar pedido vazio | `IllegalStateException` | ✅ "Pedido sem itens não pode ser pago" |
| Pagar com processador `null` | `IllegalArgumentException` | ✅ "Forma de pagamento é obrigatória" |
| Pagar pedido já pago | Decisão da equipe: **recusa** | ✅ `IllegalStateException`: "Pedido já está pago" |
| Percorrer todas as formas em um laço | Cada uma executa o seu próprio comportamento | ✅ Pix aprova, Boleto aguarda, Cartão aprova — sem `if`, sem `instanceof` |
| Prova da extensão: acrescentar Dinheiro | Zero arquivos existentes alterados | ✅ `Dinheiro` implementa só `ProcessadorPagamento`; nenhum outro arquivo mudou |

## Critério para `false` vs. exceção (decisão da equipe)

| Situação | `false` ou exceção? | Argumento |
|---|---|---|
| Boleto ainda não compensado | `false` | Situação de negócio prevista — quem chama pode oferecer outra forma |
| Cartão com mais de 12 parcelas | Exceção | Erro de uso da classe: parâmetro inválido na construção |
| Dinheiro com valor recebido insuficiente | `false` | Situação de negócio prevista (cliente pode completar o valor) |
| Processador `null` | Exceção | Erro de uso da classe (`Pedido.pagar`) |
| Pedido já pago | Exceção | Erro de uso do fluxo — decisão da equipe: não permitir novo pagamento |

**Critério geral adotado:** situação de negócio prevista → `false`; erro de uso da
classe ou do fluxo → exceção. Nenhuma forma foge desse critério.

## Prova do princípio aberto/fechado

Arquivos alterados para acrescentar a quarta forma (`Dinheiro`): **0**.
`Dinheiro` foi criada como arquivo novo, implementando apenas
`ProcessadorPagamento` — nem precisou estender `FormaPagamento`. `Pedido`,
`Pix`, `Boleto` e `CartaoCredito` não foram tocados.

## Onde a equipe decidiu NÃO aplicar polimorfismo

`Produto`, `Cliente`, `Endereco` e `ItemPedido` continuam como classes simples,
sem hierarquia: não há variação de comportamento por tipo nessas entidades,
então uma hierarquia ali seria complicação sem necessidade.

## Validação que hoje só lança exceção sem tratamento (para a Aula 09)

Todas as `IllegalArgumentException` e `IllegalStateException` do domínio
(`Pedido`, `Produto`, `FormaPagamento` e suas subclasses) são lançadas e hoje
apenas impressas com `try/catch` no `main` — sem uma hierarquia própria de
exceções nem um tratamento centralizado. Esse é o ponto de partida da Aula 09.
