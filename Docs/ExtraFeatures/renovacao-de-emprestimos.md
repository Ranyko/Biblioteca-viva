# Extra Feature — Renovação de Empréstimos

## 1. Contexto

O leitor que precisa de mais tempo para concluir uma leitura deve devolver o exemplar ou procurar a equipe, mesmo quando não há outra pessoa aguardando a obra. A renovação permite estender o prazo de um empréstimo elegível, sem interferir na fila de reservas e mantendo o histórico necessário para explicar a cobrança de multas.

## 2. Objetivo

Permitir que o leitor renove os próprios empréstimos ativos ou que um atendente faça a renovação em seu nome, respeitando o limite registrado no momento da retirada e a existência de reservas para a obra. Cada mudança de prazo deve ser auditável, identificar quem a realizou e preservar as datas anteriores.

## 3. Perfis e responsabilidades

| Perfil | Responsabilidades |
|---|---|
| Leitor | Renovar empréstimo próprio elegível e consultar seu histórico de renovações |
| Atendente e administrador | Renovar empréstimo em nome do leitor, além de registrar retiradas e devoluções |

## 4. Escopo funcional

- A configuração única da biblioteca define `limite_renovacoes`, não negativo; o valor inicial é `2`.
- Na retirada, o limite vigente é copiado para `emprestimo.limite_renovacoes_snapshot`. Alterações posteriores na configuração não modificam os empréstimos existentes.
- O leitor pode renovar o próprio empréstimo, e o atendente ou administrador pode renová-lo em nome do leitor, até o limite associado àquela retirada, desde que ainda esteja ativo, não vencido e não exista reserva `ativa` ou `disponivel` para o livro.
- A data prevista de devolução é estendida pelo prazo de empréstimo vigente na biblioteca, contado a partir da data prevista atual. O vencimento é permitido no próprio dia da data prevista; depois desse dia, a renovação é recusada.
- Cada renovação é registrada em histórico append-only com número sequencial, leitor, atendente responsável quando a operação é feita no balcão, data anterior, nova data e instante da operação. O limite usado é obtido de `emprestimo.limite_renovacoes_snapshot`, sem duplicá-lo no histórico. A renovação pelo próprio leitor mantém o campo do atendente nulo.
- Retirada e devolução continuam registrando, respectivamente, `atendente_retirada_id` e `atendente_devolucao_id`; cada registro aponta para o usuário autenticado que executou a operação.
- A renovação e a gravação do histórico ocorrem na mesma transação. Se uma delas falhar, nenhuma alteração é persistida.
- Para evitar a corrida entre uma renovação e a criação de reserva, a aplicação serializa as operações pelo bloqueio da linha do livro. Serviços que criarem ou alterarem reservas devem obter esse bloqueio antes da gravação; a aplicação não depende de triggers ou functions no PostgreSQL.
- O histórico de renovações é append-only na aplicação: o repositório oferece gravação de novos eventos, sem operações de edição ou exclusão.

Empréstimos existentes quando a migration é aplicada recebem snapshot `0`, pois o limite histórico vigente na retirada não pode ser recuperado. Em novas instalações, o limite de exemplo é `2`.

## 5. Regras de negócio

| Código | Regra |
|---|---|
| RE-01 | O leitor dono pode renovar e consultar o próprio empréstimo; atendente e administrador podem renovar e consultar empréstimos pelo fluxo de atendimento. |
| RE-02 | Empréstimo devolvido ou vencido não pode ser renovado. |
| RE-03 | Empréstimo com reserva `ativa` ou `disponivel` para o mesmo livro não pode ser renovado. |
| RE-04 | O máximo de renovações é o snapshot gravado na retirada, não a configuração atual. |
| RE-05 | Cada renovação acrescenta `prazo_emprestimo_dias` à data prevista de devolução vigente. |
| RE-06 | O número de renovação é sequencial e único por empréstimo; histórico não pode ser alterado ou apagado. |
| RE-07 | A multa na devolução considera a data prevista vigente após as renovações; as datas anteriores permanecem no histórico para auditoria. |
| RE-08 | Renovação feita por atendente ou administrador registra seu ID em `atendente_renovacao_id`; o ID vem do token autenticado, nunca do corpo da requisição. |

## 6. Fluxos principais

### Renovação pelo leitor

1. O leitor autenticado solicita a renovação de um empréstimo próprio.
2. O sistema bloqueia o livro e o empréstimo, valida a situação, o prazo, as reservas e o limite snapshot.
3. O sistema grava a auditoria e estende a data prevista na mesma transação.
4. A resposta informa a data anterior, a nova data e o limite usado.

### Renovação pelo atendente

1. O atendente autenticado solicita a renovação do empréstimo do leitor.
2. O sistema aplica as mesmas validações de prazo, reservas e limite da renovação pelo leitor.
3. O sistema registra o ID do atendente autenticado junto ao evento de renovação e atualiza o prazo na mesma transação.
4. A resposta e o histórico identificam o atendente que realizou a operação.

### Consulta do histórico

1. O leitor solicita o histórico de um empréstimo próprio, ou o atendente/administrador consulta o histórico pela rota de atendimento.
2. O sistema retorna os registros ordenados pelo número da renovação, incluindo o ID do atendente quando aplicável, sem permitir alteração ou exclusão.

## 7. Interface da API

As rotas são autenticadas. As rotas `/leitor/**` exigem o perfil `leitor`; as rotas `/atendente/**` exigem `atendente` ou `administrador`.

| Método e rota | Descrição |
|---|---|
| `POST /leitor/emprestimos/{id}/renovacao` | Renova o empréstimo próprio se todas as regras forem atendidas. Não exige corpo. |
| `GET /leitor/emprestimos/{id}/renovacoes` | Lista o histórico de renovações do empréstimo próprio em ordem cronológica. |
| `POST /atendente/emprestimos/{id}/renovacao` | Renova o empréstimo do leitor em nome dele; o atendente responsável é obtido do token. Não exige corpo. |
| `GET /atendente/emprestimos/{id}/renovacoes` | Lista o histórico de renovações do empréstimo para atendimento, incluindo o atendente responsável por cada renovação. |

A resposta de uma renovação contém `id`, `emprestimoId`, `numeroRenovacao`, `limiteRenovacoesSnapshot`, `dataPrevistaAnterior`, `dataPrevistaNova`, `atendenteRenovacaoId` e `renovadaEm`. `atendenteRenovacaoId` é nulo para renovações solicitadas diretamente pelo leitor.

### Códigos HTTP de erro

| Código | Quando ocorre nesta funcionalidade |
|---|---|
| `401 Unauthorized` | Token ausente, inválido ou expirado. |
| `403 Forbidden` | Usuário autenticado sem o perfil exigido pela rota. |
| `404 Not Found` | Empréstimo inexistente ou, na rota do leitor, que pertence a outro leitor. |
| `409 Conflict` | Empréstimo devolvido ou vencido, reserva ativa/disponível, limite de renovações atingido ou alteração concorrente incompatível. |

Erros tratados pela aplicação usam o formato `{ "timestamp", "status", "erro", "mensagem", "caminho", "campos" }`.

## 8. Critérios de aceite

- [ ] A retirada registra no empréstimo o limite de renovações vigente.
- [ ] Alterar a configuração não altera o limite já associado a empréstimos existentes.
- [ ] Leitor renova somente empréstimo próprio, ativo, não vencido e sem reserva ativa/disponível para o livro.
- [ ] Cada renovação estende o prazo pela quantidade de dias configurada e respeita o snapshot do limite.
- [ ] Empréstimos vencidos, devolvidos ou no limite não podem ser renovados.
- [ ] A data anterior e a nova data ficam em um histórico imutável, inclusive após sucessivas renovações.
- [ ] Renovações feitas por atendente identificam o usuário autenticado, sem aceitar um ID de atendente fornecido pelo cliente.
- [ ] Retirada, renovação e devolução preservam os IDs dos respectivos atendentes no histórico.
- [ ] Multas são calculadas usando a data prevista vigente na devolução, sem perder o histórico de prazos anteriores.
- [ ] Requisições concorrentes não ultrapassam o limite nem renovam enquanto uma reserva é criada para o livro.

## 9. Fora de escopo

- Renovação de empréstimo vencido ou já devolvido.
- Prorrogação automática, notificações ou alterações na fila de reservas.
- Alteração retroativa de multas já geradas ou de snapshots de empréstimos existentes.
