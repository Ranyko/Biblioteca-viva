# Biblioteca Viva

API do sistema de controle de biblioteca comunitária desenvolvido na disciplina de Laboratório de Engenharia de Software. A Sprint 1 adaptou a autenticação do projeto [Auth-Guard](https://github.com/Ranyko/Auth-Guard) ao domínio e ao banco de dados do Biblioteca Viva.

## Estado atual

O backend reúne autenticação, usuários e leitores da Sprint 1 (H1–H3) e cadastro de livros, autores, categorias e exemplares, pesquisa do acervo, empréstimos e devoluções da Sprint 2 (H4–H8). H4/H5 foram replanejadas da Sprint 1 e implementadas na Sprint 2. Os resultados de cada etapa estão registrados nos documentos das respectivas sprints.

Em 02/10/2026, Raniery executou a validação local no Windows 11, com Java 21.0.4 e PostgreSQL 15.19: **111 testes automatizados aprovados, zero falhas, zero erros e zero ignorados**, incluindo 44 testes de integração com banco. Os **13 cenários Postman** de H4–H8 tiveram os retornos esperados. Consulte o [relatório da Sprint 2](Docs/Sprints/Sprint-2/sprint-2-relatorio.md) e as [evidências](Docs/Sprints/Sprint-2/sprint-2-evidencias-teste.md).

### Fundação da Sprint 1

O backend da Sprint 1 inclui autenticação, usuários e leitores. A validação local registrou PostgreSQL saudável, inicialização da API, 11 testes unitários aprovados e nove verificações HTTP aprovadas pelo script PowerShell. Os fluxos do Postman demonstram autenticação, autorização, criação/atualização/inativação de usuário, bloqueio do token anterior à inativação, cadastro de leitor e rejeição de documento duplicado.

Os resultados registrados em 23/09/2026 e 02/10/2026 foram reunidos nas [Evidências da Sprint 1](Docs/Sprints/Sprint-1/sprint-1-evidencias-teste.md), com uma tabela única e 17 arquivos de evidência.

Funcionalidades da Sprint 1 preservadas:

- login com e-mail e senha;
- senhas protegidas com BCrypt;
- emissão e validação de tokens JWT;
- autorização por perfil: `leitor`, `atendente` e `administrador`;
- bloqueio de login e de tokens pertencentes a usuários inativos;
- gerenciamento de usuários por administrador;
- cadastro e consulta de leitores por atendente ou administrador;
- validação de entrada e erros de aplicação em JSON; bloqueios do filtro de segurança retornam HTTP 401 ou 403;
- criação e versionamento do banco PostgreSQL com Flyway;
- testes unitários do login, token e filtro de segurança.

Não existe cadastro público. Funcionários são criados pelo administrador e leitores são cadastrados pelo atendente ou administrador. A integração com as telas React não foi verificada nesta entrega de backend.

## Tecnologias

- Java 21
- Spring Boot 4
- Spring Security
- Spring Data JPA
- PostgreSQL 15
- Flyway
- JWT
- Docker Compose
- Maven

## Como executar

### Pré-requisitos

- Java 21 ou superior;
- Docker Desktop com Docker Compose;
- portas `5432` e `8080` livres.

### 1. Subir o PostgreSQL

Na raiz do projeto:

```bash
docker compose up -d
```

O Compose cria o banco `biblioteca_viva`. Na primeira execução da API, o Flyway aplica `V1__schema_inicial.sql`, `V2__corrige_senha_demonstracao.sql` e `V3__indices_circulacao_acervo.sql`, em `src/main/resources/db/migration`. A V2 corrige a senha de demonstração da versão anterior. Em banco já gerenciado por Flyway com V1 aplicada, as versões seguintes ainda não registradas serão executadas; senhas já alteradas são preservadas.

Para conferir se o banco ficou saudável:

```bash
docker compose ps
```

### 2. Iniciar a API

No macOS ou Linux:

```bash
./mvnw spring-boot:run
```

No Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

A API ficará disponível em `http://localhost:8080`.

Os valores padrão são exclusivos da demonstração local. Antes do deploy, configure as variáveis descritas em `.env.example`, principalmente `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` e `CORS_ALLOWED_ORIGINS`.

O Docker Compose lê `.env`, mas a API iniciada pelo Maven não importa esse arquivo automaticamente. Configure as mesmas variáveis no terminal ou na IDE que inicia a API. Exemplo: `$env:DB_PASSWORD = "sua-senha"` no PowerShell ou `export DB_PASSWORD="sua-senha"` no macOS/Linux. Use JDK 21, incluindo compilador, para reproduzir a configuração do projeto.

Se a porta 5432 já estiver ocupada pelo banco da E3, use `5433:5432` no Compose e `DB_URL=jdbc:postgresql://localhost:5433/biblioteca_viva` na API. O volume deste Compose guarda os dados entre reinicializações. Não execute o SQL avulso junto com o Flyway. Para um banco da E3 preenchido manualmente, use inicialmente um banco novo: não ativamos baseline automático, pois ele poderia pular alterações necessárias.

## Usuários de demonstração

Todos usam a senha `password`.

| Perfil | E-mail |
|---|---|
| Administrador | `admin@biblioteca.com` |
| Atendente | `carlos@biblioteca.com` |
| Atendente | `ana@biblioteca.com` |
| Leitor | `joao@biblioteca.com` |
| Leitor | `maria@biblioteca.com` |

Os dados existem apenas para desenvolvimento e demonstração. Devem ser removidos ou substituídos antes de um ambiente real.

## Testando o login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@biblioteca.com","senha":"password"}'
```

A resposta contém um token:

```json
{
  "token": "eyJ...",
  "tipo": "Bearer",
  "usuarioId": 1,
  "nome": "Administrador",
  "email": "admin@biblioteca.com",
  "perfil": "administrador"
}
```

Use esse valor nas rotas protegidas:

```bash
curl http://localhost:8080/admin/painel \
  -H "Authorization: Bearer SEU_TOKEN"
```

## Endpoints da Sprint 1

| Método | Endpoint | Acesso | Finalidade |
|---|---|---|---|
| `POST` | `/auth/login` | Público | Autenticar e emitir JWT |
| `GET` | `/auth/me` | Autenticado | Consultar o usuário atual |
| `GET` | `/admin/painel` | Administrador | Verificar acesso administrativo |
| `GET` | `/admin/usuarios` | Administrador | Listar usuários |
| `POST` | `/admin/usuarios` | Administrador | Criar atendente ou administrador |
| `PUT` | `/admin/usuarios/{id}` | Administrador | Editar dados, perfil compatível e situação |
| `GET` | `/atendente/painel` | Atendente ou administrador | Verificar acesso operacional |
| `GET` | `/atendente/leitores` | Atendente ou administrador | Listar leitores |
| `POST` | `/atendente/leitores` | Atendente ou administrador | Criar usuário e cadastro de leitor |
| `GET` | `/leitor/painel` | Leitor | Verificar acesso do leitor |

Exemplo de criação de atendente:

```json
{
  "nome": "Novo Atendente",
  "email": "atendente@biblioteca.com",
  "senha": "senha-segura",
  "perfil": "atendente",
  "ativo": true
}
```

Exemplo de cadastro de leitor:

```json
{
  "nome": "Novo Leitor",
  "email": "leitor@biblioteca.com",
  "senha": "senha-segura",
  "documento": "12345678901",
  "telefone": "(16) 99999-9999"
}
```

## Contrato do backend H4 a H8

Todas as chamadas abaixo exigem `Authorization: Bearer <token>`. `POST` de cadastro retorna **201** e `Location`; consultas/edições/devolução retornam **200**. Erros: **400** dados inválidos; **401** sem autenticação; **403** perfil sem autorização; **404** cadastro inexistente; **409** duplicidade ou conflito de negócio.

| Métodos e rota | Permissão | Entrada/observação |
|---|---|---|
| `GET/POST /admin/autores`; `GET/PUT /admin/autores/{id}` | Administrador | POST/PUT `{ "nome": "Nome" }`; máximo 160 caracteres |
| `GET/POST /admin/categorias`; `GET/PUT /admin/categorias/{id}` | Administrador | POST/PUT `{ "nome": "Nome" }`; máximo 80 caracteres; nome único conforme o schema |
| `GET/POST /admin/livros`; `GET/PUT /admin/livros/{id}` | Administrador | Corpo abaixo; consulta administrativa inclui livros inativos |
| `GET/POST /admin/exemplares`; `GET/PUT /admin/exemplares/{id}` | Administrador | Listagem aceita filtro `livroId`; vínculo com livro é imutável |
| `PATCH /admin/exemplares/{id}/situacao` | Administrador | `{ "ativo": false }` inativa; `true` disponibiliza, se obra ativa e sem movimentação ativa |
| `GET /acervo` | Qualquer perfil autenticado | `titulo`, `autor`, `categoria` opcionais; apenas obras ativas |
| `POST /atendente/emprestimos` | Atendente ou administrador | `{ "leitorId": 2, "exemplarId": 1 }` |
| `GET /atendente/emprestimos`; `GET /atendente/emprestimos/{id}` | Atendente ou administrador | Lista padrão só ativos; `ativos=false` inclui devolvidos |
| `POST /atendente/emprestimos/{id}/devolucao` | Atendente ou administrador | Sem corpo; retorna movimentação, atraso, valor da multa e destinação do exemplar |

Listagens aceitam `pagina=0` e `tamanho=20` (máximo 100). Autores/categorias retornam uma lista paginada; livros/exemplares/acervo retornam `{itens,pagina,tamanho,total}`; empréstimos retornam lista paginada.

Livro — POST/PUT:

```json
{
  "titulo": "Obra de teste",
  "isbn": "9781234567890",
  "anoPublicacao": 2026,
  "descricao": "Descrição opcional",
  "ativo": true,
  "autorIds": [1, 2],
  "categoriaIds": [1]
}
```

ISBN: remover espaços/hífens, converter `x` para `X`; aceitar 13 dígitos ou 10 caracteres (último pode ser `X`). O critério desta entrega é formato e unicidade; não há validação do dígito verificador. Ano opcional entre 1000 e 32767, conforme SMALLINT do schema. Arrays de vínculos aceitam no máximo 50 IDs positivos distintos cada. **PUT representa o cadastro completo:** arrays substituem os vínculos anteriores; omitir arrays equivale a `[]`, e omitir `ativo` equivale a `true`. Enviar todos os campos que deseja manter.

Exemplar — POST:

```json
{ "livroId": 1, "codigoTombo": "NOVO-001", "estadoConservacao": "bom" }
```

Conservação: `novo`, `bom`, `regular`, `danificado`. O servidor define `status: "disponivel"`; não aceitar status vindo do cliente como comando de circulação. PUT recebe somente `codigoTombo` e `estadoConservacao`. Tombo e nomes são aparados; unicidade segue a comparação do schema. Homônimos de autores são permitidos. Não criar exemplar em livro inativo.

### Transações e circulação

- Ordem compartilhada de travas: **livro → exemplar**, seguida de usuário/leitor na retirada ou empréstimo/reserva na devolução. Alterações manuais de exemplar seguem essa mesma ordem.
- Módulos que alterem fila/alocação de reservas precisam travar o mesmo livro antes das alterações. A gestão completa de reservas continua fora deste complemento.
- H7 aceita exemplar reservado somente para seu titular e antes do prazo; a reserva passa a atendida. Esse comportamento corresponde ao cenário de retirada de reserva própria previsto no plano de testes.
- H8 aplica `mínimo(dias × diária, teto)`, preserva diária/teto aplicados e não cria multa pendente de valor zero. A geração de multa na devolução não implementa consulta/baixa de multas.
- Datas e funcionário responsável vêm do servidor/token. `leitorId` é o ID da tabela leitor, distinto de `usuarioId`. Fuso padrão: `America/Sao_Paulo`.

## Como a autenticação funciona

1. O cliente envia e-mail e senha para `/auth/login`.
2. A API busca o usuário por e-mail e compara a senha com o hash BCrypt.
3. Se o usuário estiver ativo, a API assina um JWT válido por duas horas.
4. Nas próximas chamadas, o cliente envia `Authorization: Bearer <token>`.
5. O filtro valida assinatura, emissor e expiração, recarrega o usuário pelo ID imutável no banco e aplica seu perfil ao Spring Security.
6. O Spring Security libera ou bloqueia a rota. Falta de autenticação produz `401`; perfil incompatível produz `403`.

O perfil é recarregado do banco a cada requisição protegida. Portanto, desativar um usuário ou alterar seu perfil produz efeito mesmo que ele ainda possua um token antigo.

## Testes

No macOS ou Linux:

```bash
./mvnw test
```

No Windows:

```powershell
.\mvnw.cmd test
```

Os testes originais da Sprint 1 cobrem credenciais válidas e inválidas, usuário inativo, assinatura e expiração do JWT, identificação pelo ID, carregamento de autoridade pelo filtro e compatibilidade da senha de demonstração com BCrypt. O resultado registrado na Sprint 1 é de 11 testes aprovados, sem falhas ou erros.

As [Evidências da Sprint 1](Docs/Sprints/Sprint-1/sprint-1-evidencias-teste.md) registram os testes de autenticação e autorização, os retornos HTTP 200 na atualização e inativação de usuário, HTTP 401 com o token anterior à inativação, HTTP 201 no cadastro de leitor e HTTP 409 para documento duplicado.

Os fluxos HTTP demonstrados estão reunidos na [coleção Postman da Sprint 1](Docs/Postman/sprint-1.postman_collection.json), com os resultados registrados nas evidências.

### Suíte H4–H8

A execução local de 02/10/2026 aprovou os 111 casos. Com o banco descartável configurado, `./mvnw clean verify` (macOS/Linux) ou `.\mvnw.cmd clean verify` (PowerShell) executa a suíte: 67 de serviço/MVC e 44 de PostgreSQL, incluindo os testes originais e regressões da Sprint 1. Sem `SPRINT2_TEST_DB_URL`, os 44 casos de banco são ignorados. Usar exclusivamente o banco de testes, pois a fixture usa TRUNCATE.

### Testes de integração com PostgreSQL

Criar um banco novo e descartável:

```bash
docker run --rm -d --name biblioteca-sprint2-test -p 55432:5432 -e POSTGRES_DB=biblioteca_sprint2_test -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres postgres:15
docker exec biblioteca-sprint2-test pg_isready -U postgres
```

Após o banco responder:

```bash
SPRINT2_TEST_DB_URL=jdbc:postgresql://localhost:55432/biblioteca_sprint2_test \
SPRINT2_TEST_DB_USER=postgres SPRINT2_TEST_DB_PASSWORD=postgres ./mvnw verify
```

Windows PowerShell:

```powershell
$env:SPRINT2_TEST_DB_URL="jdbc:postgresql://localhost:55432/biblioteca_sprint2_test"
$env:SPRINT2_TEST_DB_USER="postgres"
$env:SPRINT2_TEST_DB_PASSWORD="postgres"
.\mvnw.cmd verify
```

**A suíte usa TRUNCATE antes de cada caso. Nunca apontar para o banco da biblioteca.** A URL deve conter um banco terminado em `_sprint2_test`. Depois, `docker stop biblioteca-sprint2-test` remove apenas esse container descartável, criado com `--rm`.

As classes `CirculacaoPostgresTest` e `CatalogoPostgresTest` contêm casos para SQL, Flyway, autorização/JWT, vínculos, duplicidade, rollback, concorrência e o fluxo cadastrar obra/exemplar → emprestar → devolver. Também incluem quatro regressões dos fluxos da Sprint 1. A configuração `.github/workflows/backend-testes.yml` usa Java 21 + PostgreSQL 15; os resultados locais estão registrados nas evidências da sprint.

### Coleção Postman da Sprint 2

Importar [sprint-2-h4-h8.postman_collection.json](Docs/Postman/sprint-2-h4-h8.postman_collection.json) e executar os 13 cenários na ordem. A coleção usa `baseUrl=http://localhost:8080` e guarda token e IDs automaticamente. A variável `leitorId=2` deve corresponder a um leitor ativo e dentro do limite de empréstimos. Os resultados estão nas [evidências da Sprint 2](Docs/Sprints/Sprint-2/sprint-2-evidencias-teste.md).

## Documentos das sprints

- [Relatório da Sprint 1](Docs/Sprints/Sprint-1/sprint-1-relatorio.md)
- [Backlog da E2](Docs/E1-E4/backlog-biblioteca-viva%28E2%29.md)
- [Evidências de teste](Docs/Sprints/Sprint-1/sprint-1-evidencias-teste.md)
- [Retrospectiva individual](Docs/Sprints/Sprint-1/sprint-1-retrospectiva.md)
- [Relatório individual de Raniery](Docs/Sprints/Sprint-1/sprint-1-contribuicao-raniery.md)
- [Relatório da Sprint 2](Docs/Sprints/Sprint-2/sprint-2-relatorio.md)
- [Evidências da Sprint 2](Docs/Sprints/Sprint-2/sprint-2-evidencias-teste.md)
- [Contribuição individual da Sprint 2](Docs/Sprints/Sprint-2/sprint-2-contribuicao-raniery.md)
- [Retrospectiva individual da Sprint 2](Docs/Sprints/Sprint-2/sprint-2-retrospectiva.md)

## Estrutura principal

```text
src/main/java/br/com/bibliotecaviva/
├── config/       # configuração do Spring Security, CORS e Clock
├── controller/   # endpoints HTTP
├── dto/          # dados de entrada e saída
├── exception/    # erros padronizados
├── model/        # entidades e perfis
├── repository/   # acesso ao PostgreSQL
├── security/     # filtro JWT
└── service/      # autenticação, cadastros, pesquisa e circulação
```

O arquivo `db/schema.sql` contém o DDL e o seed atualizado para um banco vazio. Na aplicação, use apenas as migrações em `src/main/resources/db/migration`. A V1 original foi preservada para manter seu checksum; a V2 corrige o hash sem recriar tabelas. Por isso, o SQL avulso representa o resultado de V1 + V2 e não é idêntico à V1.

O histórico da Sprint 1 está no [relatório correspondente](Docs/Sprints/Sprint-1/sprint-1-relatorio.md). O estado H4–H8 está no [Relatório da Sprint 2](Docs/Sprints/Sprint-2/sprint-2-relatorio.md).

## Alcance da validação

Os resultados documentados das Sprints 1 e 2 abrangem o backend H1–H8, seus testes automatizados e os cenários HTTP demonstrados. Os testes de circulação com reservas utilizam dados preparados no banco; a integração completa com os endpoints H9/H10, as telas e o ambiente de deploy não é demonstrada por essas evidências. O registro se limita aos cenários efetivamente executados.

## Equipe

- Raniery Chiarelli — RA 2840482321007
- Vinicius Rocha — RA 2840482523051
- Isaac Leonardo da Silva — RA 2840482421016

## Licença

Distribuído sob a licença MIT. Consulte `LICENSE`.
