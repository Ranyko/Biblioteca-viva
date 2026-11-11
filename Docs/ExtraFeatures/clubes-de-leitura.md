# Extra Feature — Clubes de Leitura

## 1. Contexto

O Biblioteca Viva organiza o acervo e as operações de empréstimo, mas não oferece recursos para que leitores se encontrem em torno de uma leitura em comum. Em uma biblioteca comunitária, clubes de leitura podem incentivar a participação, aproximar leitores e dar visibilidade a obras que talvez não fossem descobertas em uma pesquisa individual.

Essa funcionalidade complementa o catálogo e as sugestões existentes: seu foco é organizar atividades coletivas, não recomendar livros automaticamente nem alterar regras de empréstimo e reserva.

## 2. Objetivo

Permitir que a equipe da biblioteca crie clubes de leitura, programe encontros associados a obras do acervo e acompanhe as inscrições dos leitores. O leitor poderá encontrar clubes ativos, consultar seus próximos encontros e inscrever-se ou cancelar sua participação.

## 3. Perfis e responsabilidades

| Perfil | Responsabilidades |
|---|---|
| Administrador | Criar, editar, encerrar e reativar clubes; gerenciar encontros e consultar participantes |
| Atendente | Criar e atualizar encontros dos clubes e registrar presença |
| Leitor | Consultar clubes e encontros ativos, inscrever-se, cancelar inscrição e consultar suas participações |

## 4. Escopo funcional

### 4.1 Clubes

- Cada clube possui nome, descrição, situação (ativo ou encerrado) e responsável da biblioteca.
- O administrador pode definir a capacidade máxima de participantes e a periodicidade desejada dos encontros.
- Clubes encerrados permanecem consultáveis pela equipe para preservar seu histórico, mas não aceitam novas inscrições.
- A listagem para leitores apresenta apenas clubes ativos, com filtros por nome e categoria de interesse.

### 4.2 Encontros

- Um clube pode ter vários encontros, cada um com data e hora, local ou instrução de acesso, situação e uma obra em discussão.
- A obra do encontro deve ser selecionada entre os livros cadastrados no acervo; associá-la ao encontro não reserva nem empresta exemplares.
- Encontros podem ser abertos, cancelados ou concluídos. Encontros cancelados não aceitam inscrições e permanecem no histórico.
- Leitores podem consultar o calendário e os detalhes dos encontros de clubes ativos.

### 4.3 Inscrições e presença

- Um leitor ativo pode inscrever-se em um encontro aberto enquanto houver vagas.
- A inscrição é única por leitor e encontro. O leitor pode cancelá-la antes do início do encontro.
- Quando a capacidade for atingida, novas inscrições são recusadas; não há fila de espera nesta proposta.
- A partir do início do encontro, o atendente pode registrar a presença ou ausência dos inscritos. O registro não altera empréstimos, multas ou a situação cadastral do leitor.
- O leitor consulta seus encontros futuros e seu histórico de participações.

## 5. Regras de negócio

| Código | Regra |
|---|---|
| CL-01 | Somente administradores podem criar, encerrar ou reativar clubes. |
| CL-02 | Somente clubes ativos podem receber encontros abertos para inscrição. |
| CL-03 | A capacidade máxima deve ser positiva; não se pode excedê-la com inscrições confirmadas. |
| CL-04 | Um leitor não pode inscrever-se mais de uma vez no mesmo encontro. |
| CL-05 | Apenas leitores ativos podem inscrever-se; a inscrição pode ser cancelada pelo próprio leitor antes do início. |
| CL-06 | Não se pode inscrever em encontro cancelado, concluído ou já iniciado. |
| CL-07 | A obra associada deve existir no acervo, mas não precisa ter exemplar disponível para empréstimo. |
| CL-08 | Somente atendentes e administradores podem registrar presença, e somente para leitores inscritos. |
| CL-09 | Encerrar um clube impede novas atividades e inscrições, sem apagar encontros ou presenças já registrados. |

## 6. Fluxos principais

### Inscrição de um leitor

1. O leitor consulta os clubes ativos e escolhe um encontro aberto.
2. O sistema verifica se o leitor está ativo, se ainda há vaga e se não existe inscrição anterior.
3. Se as verificações forem aprovadas, o sistema registra a inscrição e atualiza a quantidade de vagas disponíveis.
4. O leitor pode cancelar a inscrição antes do início, liberando a vaga para outra pessoa.

### Organização e realização de um encontro

1. O administrador cria um clube e define suas informações básicas.
2. Um administrador ou atendente agenda um encontro, informa local e horário e associa uma obra do acervo.
3. Leitores consultam o encontro e fazem suas inscrições até o limite definido.
4. A partir do início do encontro, o atendente registra a presença ou ausência dos inscritos; após todos os registros, a atividade pode ser concluída.

## 7. Interface da API

As rotas abaixo são autenticadas e usam os prefixos de perfil do sistema.

### 7.1 Clubes — administrador

| Método e rota | Descrição |
|---|---|
| `GET /admin/clubes-leitura` | Lista clubes, incluindo encerrados. Filtros opcionais: `nome`, `categoriaId`, `pagina` e `tamanho`. |
| `GET /admin/clubes-leitura/{id}` | Consulta os dados de um clube. |
| `POST /admin/clubes-leitura` | Cria um clube; o usuário autenticado fica registrado como responsável. Retorna `201 Created`. |
| `PUT /admin/clubes-leitura/{id}` | Atualiza os dados do clube e substitui seus vínculos de categoria. |
| `PATCH /admin/clubes-leitura/{id}/status` | Encerra ou reativa um clube. |

O corpo para criação e atualização segue este formato:

```json
{
  "nome": "Clube de leitura contemporânea",
  "descricao": "Encontros mensais para conversar sobre literatura.",
  "capacidadeMaxima": 12,
  "periodicidade": "Mensal",
  "categoriaIds": [1, 3]
}
```

`nome` é obrigatório e aceita até 160 caracteres; `capacidadeMaxima` é obrigatória e deve estar entre `1` e `32767`; `periodicidade` aceita até 80 caracteres; `categoriaIds` é opcional, aceita até 50 IDs positivos e distintos. `descricao` é opcional.

Para alterar a situação, envie:

```json
{ "status": "encerrado" }
```

Os valores aceitos são `ativo` e `encerrado`. A resposta do clube contém `id`, `nome`, `descricao`, `status`, `capacidadeMaxima`, `periodicidade`, `responsavelId`, `criadoEm` e `categorias` (lista de referências com `id` e `nome`).

### 7.2 Clubes e encontros — equipe

As operações estão disponíveis para atendentes e administradores.

| Método e rota | Descrição |
|---|---|
| `GET /atendente/clubes-leitura` | Lista clubes, incluindo encerrados. Filtros opcionais: `nome`, `categoriaId`, `pagina` e `tamanho`. |
| `GET /atendente/clubes-leitura/{clubeId}/encontros` | Lista encontros do clube, com `pagina` e `tamanho` opcionais. |
| `POST /atendente/clubes-leitura/{clubeId}/encontros` | Agenda um encontro. Retorna `201 Created`. |
| `PUT /atendente/clubes-leitura/{clubeId}/encontros/{encontroId}` | Atualiza um encontro aberto e ainda não iniciado. |
| `PATCH /atendente/clubes-leitura/{clubeId}/encontros/{encontroId}/cancelar` | Cancela um encontro aberto que ainda não começou. |
| `PATCH /atendente/clubes-leitura/{clubeId}/encontros/{encontroId}/concluir` | Conclui um encontro iniciado, após o registro de presença ou ausência de todas as inscrições confirmadas. |
| `GET /atendente/clubes-leitura/{clubeId}/encontros/{encontroId}/inscricoes` | Lista participantes e informações de presença; aceita `pagina` e `tamanho`. |
| `PATCH /atendente/clubes-leitura/{clubeId}/encontros/{encontroId}/inscricoes/{leitorId}/presenca` | Registra ou atualiza a presença de um leitor inscrito, após o início do encontro. |

O corpo para criar ou atualizar encontro:

```json
{
  "livroId": 42,
  "dataHora": "2026-12-10T18:30:00-03:00",
  "localOuAcesso": "Sala de leitura"
}
```

Todos os campos são obrigatórios. `livroId` deve identificar um livro existente, `dataHora` deve estar no futuro ao criar ou atualizar, e `localOuAcesso` aceita até 1000 caracteres.

Para presença, envie `{ "presente": true }` ou `{ "presente": false }`. A resposta do encontro contém `id`, `clubeId`, `clubeNome`, `livroId`, `livroTitulo`, `dataHora`, `localOuAcesso`, `status`, `capacidadeMaxima`, `inscricoesConfirmadas`, `vagasDisponiveis` e `criadoEm`. A resposta de cada participante contém `leitorId`, `nome`, `statusInscricao`, `inscritoEm`, `presente` e `presencaRegistradaEm`.

### 7.3 Consulta, inscrições e histórico — leitor

| Método e rota | Descrição |
|---|---|
| `GET /leitor/clubes-leitura` | Lista somente clubes ativos. Filtros opcionais: `nome`, `categoriaId`, `pagina` e `tamanho`. |
| `GET /leitor/clubes-leitura/{clubeId}/encontros` | Lista encontros de um clube ativo, com `pagina` e `tamanho` opcionais. |
| `GET /leitor/clubes-leitura/{clubeId}/encontros/{encontroId}` | Consulta os detalhes de um encontro de clube ativo. |
| `POST /leitor/encontros-clube/{encontroId}/inscricao` | Inscreve o leitor autenticado em um encontro. Retorna `201 Created`. |
| `DELETE /leitor/encontros-clube/{encontroId}/inscricao` | Cancela a própria inscrição antes do início do encontro. |
| `GET /leitor/minhas-inscricoes` | Consulta as próprias inscrições; aceita `historico`, `pagina` e `tamanho`. `historico` é `false` por padrão. |

Inscrição e cancelamento não exigem corpo. A inscrição só é permitida a leitores ativos, em encontro aberto de clube ativo, antes do início e enquanto houver vaga; uma inscrição anterior, mesmo cancelada, impede nova inscrição para o mesmo encontro. `historico=false` retorna inscrições confirmadas para encontros futuros de clubes ativos; `historico=true` retorna inscrições canceladas ou vinculadas a encontros passados, cancelados ou concluídos.

A resposta de uma inscrição contém `id`, `encontroId`, `leitorId`, `status`, `inscritoEm`, `canceladoEm`, `presente` e `presencaRegistradaEm`. A listagem de inscrições próprias também inclui dados do clube e do livro, data e local do encontro, status do encontro e status da inscrição.

### 7.4 Situações e validações

- Situações de clube: `ativo` e `encerrado`; de encontro: `aberto`, `cancelado` e `concluido`; de inscrição: `confirmada` e `cancelada`.
- IDs de recursos devem ser positivos. Paginação inválida e corpos que não atendem às validações são rejeitados.
- Inscrições excedentes, duplicadas ou fora das condições de participação são recusadas; alterações incompatíveis com o estado do encontro também são recusadas.
- Os erros seguem o formato JSON de erros já adotado pela API.

## 8. Critérios de aceite

- [ ] Administrador consegue criar, editar, encerrar e reativar um clube.
- [ ] Equipe consegue agendar, atualizar e cancelar encontros, associando-os a livros cadastrados.
- [ ] Leitor consegue listar clubes ativos e consultar seus encontros futuros.
- [ ] Leitor ativo consegue inscrever-se em encontro aberto com vaga e cancelar sua inscrição antes do início.
- [ ] Sistema impede inscrição duplicada, inscrição de leitor inativo e excesso de participantes.
- [ ] Atendente consegue registrar presença somente para participantes inscritos.
- [ ] Leitor consegue consultar suas inscrições futuras e seu histórico de participações.
- [ ] Encerramento de clube não remove registros históricos nem altera dados de circulação.

## 9. Modelo conceitual sugerido

| Entidade | Informações principais | Relações |
|---|---|---|
| ClubeLeitura | nome, descrição, situação, capacidade, responsável | possui encontros |
| EncontroClube | data e hora, local, situação, clube, livro | pertence a um clube e pode discutir um livro |
| InscricaoEncontro | leitor, encontro, data da inscrição, situação, presença | liga leitores a encontros |

`InscricaoEncontro` deve ter uma restrição de unicidade para o par leitor/encontro. A capacidade deve ser validada de forma consistente mesmo quando duas inscrições forem solicitadas simultaneamente.

## 10. Fora de escopo

- Fórum, comentários, avaliações ou conversas entre participantes.
- Videoconferência, transmissão ao vivo ou integração com serviços externos.
- Fila de espera para encontros lotados.
- Empréstimo automático do livro associado ou garantia de disponibilidade de exemplares.
- Recomendações automáticas de clubes ou notificações por e-mail, SMS ou WhatsApp.
- Gamificação, metas individuais de leitura e ranking de participantes.

## 11. Benefício esperado

A funcionalidade amplia o papel do Biblioteca Viva como ferramenta de uma biblioteca comunitária: além de organizar o acervo, ajuda a promover encontros presenciais em torno da leitura, mantendo o cadastro de participantes e o histórico das atividades em um só lugar.
