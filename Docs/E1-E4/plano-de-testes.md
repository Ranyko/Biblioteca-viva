# Plano de Testes — Biblioteca Viva

**Etapa:** E4 — Preparação do Projeto  
**Status:** planejamento inicial; será atualizado ao final de cada sprint

## Equipe

- Raniery Chiarelli — RA 2840482321007
- Vinicius Rocha — RA 2840482523051
- Isaac Leonardo da Silva — RA 2840482421016

**Disciplina:** Laboratório de Engenharia de Software  
**Curso:** Análise e Desenvolvimento de Sistemas — Fatec Ribeirão Preto  
**Semestre:** 2026/2  
**Trilha:** B  
**Tema:** Banco de temas nº 9 — Controle de biblioteca comunitária  

## 1. Objetivo

Este plano define como a equipe verificará os requisitos funcionais e as principais regras de negócio do Biblioteca Viva. Os testes devem manter a rastreabilidade entre as histórias do backlog, a implementação e as evidências produzidas durante as sprints.

## 2. Premissas e regras de negócio

- O sistema possui três perfis: Leitor, Atendente e Administrador.
- Apenas leitores ativos e dentro do limite configurado podem realizar empréstimos.
- Apenas leitores ativos podem realizar reservas.
- A data de retirada é preenchida pelo sistema; o prazo de devolução é calculado somando o prazo configurado, inicialmente 14 dias. Esses campos não permitem alteração manual. Mudanças na configuração afetam somente novos empréstimos.
- Um exemplar não pode estar emprestado ou reservado para duas pessoas ao mesmo tempo.
- O leitor pode reservar uma obra mesmo quando houver exemplar disponível, para retirá-lo depois. Nesse caso, o sistema vincula um exemplar à reserva, altera seu status para `reservado` e define a data limite de retirada.
- Quando não houver exemplar disponível, a reserva entra em uma fila ordenada pela data e hora da solicitação.
- Reservas que já estiverem na fila têm prioridade sobre uma nova solicitação; um exemplar disponível deve ser oferecido primeiro ao leitor elegível mais antigo.
- O mesmo leitor não pode manter duas reservas da mesma obra nos estados `ativa` (aguardando) ou `disponivel` (pronta para retirada), inclusive uma em cada estado.
- Quando o prazo de retirada expirar, a reserva deve ser encerrada e o exemplar deve ser liberado para o próximo leitor da fila ou voltar a ficar disponível.
- A multa é calculada pela fórmula `mínimo(dias de atraso × valor diário, teto configurado)` para cada empréstimo. O teto padrão é R$ 25,00; o valor diário e o teto aplicados são preservados no histórico da multa.
- Uma devolução realizada dentro do prazo não gera multa.
- O valor diário e o teto podem ser zero. Quando o total calculado for zero, nenhuma multa pendente será criada, mesmo que haja atraso; o atraso continuará registrado no histórico do empréstimo.

## 3. Estratégia de testes

| Tipo de teste | O que cobre | Ferramentas previstas | Quando executar |
|---|---|---|---|
| Unitário — backend | Cálculo de multa, validações, permissões, disponibilidade e ordem da fila | JUnit 5 e Mockito | A cada alteração de regra de negócio e em cada pull request |
| Integração — backend | Endpoints, persistência, constraints e transações com PostgreSQL | Spring Boot Test, MockMvc e Testcontainers | A partir da Sprint 2 e em cada pull request |
| Componentes — frontend | Formulários, mensagens de validação, tabelas e estados de tela | Vitest e React Testing Library | A partir da implementação do frontend |
| Manual/aceitação | Fluxos completos executados pela perspectiva de cada perfil | Roteiro documentado e navegador | Antes da demonstração de cada sprint |

## 4. Ambiente e massa de testes

- Os testes automatizados usarão configuração própria, separada do ambiente de desenvolvimento.
- Os testes de integração executarão em um banco PostgreSQL temporário e descartável.
- As senhas, tokens e endereços dos serviços serão fornecidos por variáveis de ambiente, sem credenciais reais no repositório.
- Casos dependentes de data e hora usarão um relógio controlado para evitar resultados diferentes conforme o dia da execução.
- A validação do volume de referência usará 1.000 obras, 2.000 exemplares e 500 leitores. Testes unitários e de integração poderão usar massas menores, específicas para cada cenário.
- Para os fluxos de atraso, a massa deverá conter pelo menos 100 empréstimos históricos, incluindo 15 devoluções atrasadas.
- No CT19, medir o tempo entre a confirmação da busca na interface e a exibição do resultado. Registrar a consulta, o ambiente (máquina, navegador, versões e local dos serviços), as condições de rede, se a aplicação estava iniciando ou já ativa e o tempo de cada execução. Uma execução acima de 3 segundos deve ser registrada como falha, sem ser ocultada por uma média.

## 5. Critérios de bloqueio de merge

Um pull request não poderá ser integrado à `main` quando:

1. algum teste automatizado existente falhar;
2. uma regra de negócio for criada ou alterada sem teste correspondente;
3. houver falha de autorização entre os perfis;
4. uma alteração de banco não puder ser aplicada a um PostgreSQL vazio;
5. o pull request não apresentar uma descrição da mudança e evidência compatível com seu impacto.

## 6. Casos de teste planejados

Salvo quando o cenário testar uma condição inválida, considerar usuários autenticados com o perfil autorizado, leitores ativos e dentro do limite de empréstimos, dados obrigatórios válidos e relógio controlado. Cada caso deve partir de uma base conhecida e independente dos demais. Condições alternativas de um mesmo caso devem ser executadas e registradas separadamente.

| ID | História (E2) | Cenário | Entrada ou condição | Resultado esperado | Prioridade |
|---|---:|---|---|---|---|
| CT01 | #1 | Login com credenciais inválidas | E-mail não cadastrado ou senha incorreta | Acesso recusado e mensagem genérica de credenciais inválidas | Alta |
| CT02 | #1 | Leitor tenta acessar função administrativa | Leitor autenticado acessa endpoint de gerenciamento de usuários | Requisição recusada com status HTTP 403 e nenhum dado administrativo é alterado | Alta |
| CT03 | #3 | Cadastro de leitor com documento duplicado | Documento já vinculado a outro leitor | Cadastro recusado e mensagem de documento já existente | Alta |
| CT04 | #4 | Cadastro de livro sem ISBN | Campo ISBN vazio | Cadastro recusado e indicação do campo obrigatório | Alta |
| CT05 | #7 | Empréstimo de exemplar indisponível | Exemplar com status `emprestado` ou `reservado` para outro leitor | Empréstimo recusado e estado do exemplar preservado | Alta |
| CT06 | #8 | Devolução dentro do prazo sem fila | Devolução até a data prevista e sem reserva pendente | Devolução registrada, exemplar disponível e nenhuma multa criada | Alta |
| CT07 | #8 e #11 | Devolução com atraso abaixo do teto | 5 dias de atraso, valor diário de R$ 2,50 e teto configurado de R$ 25,00 | Multa pendente de R$ 12,50 e devolução registrada | Alta |
| CT08 | #9 | Reserva de obra com exemplar disponível | Obra com pelo menos um exemplar disponível e sem fila anterior | Reserva criada, exemplar específico passa para `reservado` e recebe prazo de retirada | Alta |
| CT09 | #9 | Reserva de obra sem exemplar disponível | Todos os exemplares emprestados ou reservados | Reserva ativa criada na última posição da fila, sem exemplar vinculado | Alta |
| CT10 | #9 | Reserva duplicada | Leitor já possui reserva para a mesma obra; executar separadamente com a reserva existente em `ativa` e em `disponivel` | Nova reserva recusada em ambos os cenários; reserva existente, fila e alocação do exemplar permanecem inalteradas | Alta |
| CT11 | #9 | Duas reservas simultâneas para o último exemplar | Dois leitores solicitam ao mesmo tempo uma obra com um exemplar disponível, sem fila anterior e sem reservas existentes desses leitores para a obra | Apenas uma reserva recebe o exemplar; a outra entra na fila, sem duplicidade de alocação | Alta |
| CT12 | #9 e #10 | Fila existente tem prioridade | Há leitor aguardando e um exemplar acaba de ficar disponível quando outro leitor solicita reserva | Exemplar é destinado ao primeiro leitor elegível da fila; novo solicitante fica depois dele | Alta |
| CT13 | #10 | Devolução de obra com fila | Exemplar devolvido e duas reservas aguardando | Primeiro leitor elegível recebe o exemplar e o prazo de retirada; a ordem da fila é mantida | Alta |
| CT14 | #10 | Prazo de retirada expirado | Reserva com data limite anterior ao momento atual | Reserva marcada como expirada; exemplar vai ao próximo leitor ou volta a disponível | Alta |
| CT15 | #11 | Multa calculada acima do teto | 20 dias de atraso, valor diário de R$ 2,50 e teto configurado de R$ 25,00 | Multa limitada a R$ 25,00 | Alta |
| CT16 | #11 | Devolução no prazo | Zero dia de atraso | Nenhuma multa é criada e o valor devido permanece zero | Alta |
| CT17 | #11 e #13 | Preservação do valor histórico da multa | Alteração posterior do valor diário ou do teto configurado | Multa já gerada mantém o valor diário, o teto e o total aplicados na data da devolução | Média |
| CT18 | #13 | Configuração inválida | Prazo ou limite menor que 1; valor diário negativo; teto negativo | Alteração recusada e configuração anterior preservada | Média |
| CT19 | #6 | Consulta de disponibilidade no volume de referência | Base com 1.000 obras, 2.000 exemplares e 500 leitores; executar buscas por título, autor e categoria | Disponibilidade correta apresentada em até 3 segundos em cada execução, conforme a medição definida na seção 4 | Média |
| CT20 | #14 | Indicadores do painel | Massa contendo empréstimos ativos, atrasados e categorias distintas | Painel mostra os quatro indicadores previstos e valores compatíveis com a base | Média |
| CT21 | #7 | Cálculo automático das datas do empréstimo | Relógio controlado em 11/09/2026 e prazo configurado de 14 dias | Sistema registra a retirada em 11/09/2026 e o prazo de devolução em 25/09/2026, sem permitir alteração manual | Alta |
| CT22 | #9 | Leitor inativo tenta reservar uma obra | Leitor com situação `inativo` | Reserva recusada e disponibilidade ou fila permanece inalterada | Alta |
| CT23 | #8, #11 e #13 | Devolução atrasada com valor de multa zero | Executar separadamente: (a) 5 dias de atraso, diária de R$ 2,50 e teto zero; (b) 5 dias de atraso, diária zero e teto de R$ 25,00 | Devolução e atraso registrados; valor devido zero e nenhuma multa pendente criada em ambos os cenários | Alta |

## 7. Fluxos de aceitação

Ao final das sprints correspondentes, a equipe deverá validar pelo menos estes fluxos completos:

1. administrador cadastra a obra e o exemplar; leitor consulta o acervo; atendente registra o empréstimo; atendente registra a devolução no prazo;
2. leitor reserva uma obra disponível; exemplar fica separado; atendente registra a retirada dentro do prazo;
3. leitor entra na fila de uma obra indisponível; ocorre uma devolução; o primeiro da fila recebe o exemplar;
4. reserva não retirada expira; o sistema oferece o exemplar ao próximo leitor ou o libera;
5. devolução atrasada com valor diário e teto positivos gera multa pelo valor diário, respeitando o teto configurado;
6. usuário de cada perfil tenta acessar funções permitidas e proibidas, confirmando as autorizações.

## 8. Registro de execução e evidências

Cada execução manual ou automatizada deverá registrar:

| Campo | Conteúdo esperado |
|---|---|
| Caso | Identificador do caso de teste |
| Data e responsável | Quando e por quem foi executado |
| Versão | Commit ou pull request testado |
| Resultado | Aprovado, reprovado ou bloqueado |
| Evidência | Saída da automação, captura de tela ou descrição objetiva |
| Defeito relacionado | Link da issue, quando houver falha |
