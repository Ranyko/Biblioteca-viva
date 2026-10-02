# Evidências de Teste — Sprint 1 — Biblioteca Viva

**Equipe:** Raniery Chiarelli (RA 2840482321007) — Vinicius Rocha (RA 2840482523051) — Isaac Leonardo da Silva (RA 2840482421016)

**Datas das execuções registradas:** 23/09/2026 e 02/10/2026

**Atualização do documento:** 02/10/2026

**Responsável pela execução e pelas capturas:** Raniery Chiarelli

**Branch:** [`feature/backend-sprint-1`](https://github.com/Ranyko/Biblioteca-viva/tree/feature/backend-sprint-1)

**Referência do código:** [`787a86c`](https://github.com/Ranyko/Biblioteca-viva/commit/787a86c51cc8a15ad10d055d840b2e6e02918ccb) — código funcional introduzido em [`b5c9803`](https://github.com/Ranyko/Biblioteca-viva/commit/b5c9803)


## 1. Ambiente validado

- API Spring Boot executada localmente em `http://localhost:8080`;
- PostgreSQL 15 executado pelo Docker Compose;
- banco `biblioteca_viva` criado e versionado pelo Flyway;
- testes unitários executados pelo Maven Wrapper;
- verificações HTTP pelo script PowerShell e testes manuais pelo Postman;
- ambientes utilizados: Windows PowerShell e macOS.

## 2. Testes e evidências da Sprint 1

| ID | Verificação | Tipo | Resultado | Evidência |
|---|---|---|---|---|
| INFRA01 | Container PostgreSQL em execução e com estado `healthy` | Infraestrutura | Aprovado | [01-docker-postgres-healthy.png](../../Evidencias/Sprint-1/01-docker-postgres-healthy.png) |
| INFRA02 | Criação das 12 tabelas de domínio e da tabela `flyway_schema_history` | Banco de dados | Aprovado | [02-tabelas-banco-criadas.png](../../Evidencias/Sprint-1/02-tabelas-banco-criadas.png) |
| AUTO01 | Execução da suíte automatizada | JUnit + Maven | Aprovado: 11 testes, 0 falhas, 0 erros e `BUILD SUCCESS` | [03-testes-unitarios-build-success.png](../../Evidencias/Sprint-1/03-testes-unitarios-build-success.png) |
| AUTO02 | Roteiro automatizado de autenticação e autorização da API | PowerShell + API | Aprovado: 9 verificações HTTP concluídas | [04-script-testes-api-aprovado.png](../../Evidencias/Sprint-1/04-script-testes-api-aprovado.png) |
| AU01 | Login válido de administrador gera JWT e identifica o perfil | API/Postman | Aprovado: HTTP 200 | [05-login-administrador-200.png](../../Evidencias/Sprint-1/05-login-administrador-200.png) |
| AU02 | Administrador autenticado acessa o painel administrativo | API/Postman | Aprovado: HTTP 200 | [06-admin-acesso-painel-200.png](../../Evidencias/Sprint-1/06-admin-acesso-painel-200.png) |
| AU03 | Administrador cria um usuário atendente sem retornar a senha | API/Postman | Aprovado: HTTP 201 | [07-admin-criacao-usuario-201.png](../../Evidencias/Sprint-1/07-admin-criacao-usuario-201.png) |
| AU04 | Usuário recém-criado aparece na listagem administrativa | API/Postman | Aprovado: HTTP 200 | [08-usuario-criado-confirmacao.png](../../Evidencias/Sprint-1/08-usuario-criado-confirmacao.png) |
| CT02 | Leitor tenta acessar uma função administrativa | API/Postman | Aprovado: acesso recusado com HTTP 403 | [09-leitor-acesso-admin-403.png](../../Evidencias/Sprint-1/09-leitor-acesso-admin-403.png) |
| AU05 | Requisição sem token tenta acessar o painel administrativo | API/Postman | Aprovado: acesso recusado com HTTP 401 | [10-acesso-admin-sem-token-401.png](../../Evidencias/Sprint-1/10-acesso-admin-sem-token-401.png) |
| CT01 | Login com senha incorreta | API/Postman | Aprovado: acesso recusado com HTTP 401 e mensagem genérica | [11-login-senha-incorreta-401.png](../../Evidencias/Sprint-1/11-login-senha-incorreta-401.png) |
| AU06 | Atualização de usuário por `PUT /admin/usuarios/{id}` | API/Postman | Aprovado: HTTP 200, mesmo usuário `id: 6`, nome atualizado e `ativo: true` | [12-usuario-atualizado-200.png](../../Evidencias/Sprint-1/12-usuario-atualizado-200.png) |
| AU07 | Inativação do atendente de teste por `PUT /admin/usuarios/{id}` | API/Postman | Aprovado: HTTP 200, mesmo usuário `id: 6` e `ativo: false` | [13-usuario-inativado-200.png](../../Evidencias/Sprint-1/13-usuario-inativado-200.png) |
| AU08 | Acesso a `GET /atendente/painel` com o token anterior à inativação | API/Postman | Aprovado: acesso recusado com HTTP 401 usando `{{tokenAntigo}}` após a inativação | [14-token-anterior-inativacao-bloqueado-401.png](../../Evidencias/Sprint-1/14-token-anterior-inativacao-bloqueado-401.png) |
| LE01 | Cadastro de leitor por `POST /atendente/leitores` | API/Postman | Aprovado: HTTP 201, leitor `id: 3`, `usuarioId: 7`, ativo e resposta sem senha/hash | [15-leitor-cadastrado-201.png](../../Evidencias/Sprint-1/15-leitor-cadastrado-201.png) |
| CT03 | Cadastro de leitor com documento duplicado e e-mail diferente | API/Postman | Aprovado: HTTP 409 e mensagem `Já existe um leitor com este documento` | [16-leitor-documento-duplicado-409.png](../../Evidencias/Sprint-1/16-leitor-documento-duplicado-409.png) |
| PREP01 | Criação do atendente usado nos testes por `POST /admin/usuarios` | API/Postman | Aprovado: HTTP 201, usuário `id: 6`, perfil `atendente` e ativo | [17-atendente-teste-criado-201.png](../../Evidencias/Sprint-1/17-atendente-teste-criado-201.png) |

Os casos CT01, CT02 e CT03 mantêm os identificadores definidos no [Plano de Testes](../../E1-E4/plano-de-testes.md). Os identificadores `INFRA`, `AUTO`, `AU`, `LE` e `PREP` complementam o plano com verificações específicas da fundação técnica da Sprint 1.

## 3. Cobertura automatizada

| Classe | Quantidade | Comportamentos verificados |
|---|---:|---|
| `AuthServiceTest` | 4 | login válido, e-mail inexistente, senha incorreta e usuário inativo |
| `TokenServiceTest` | 4 | geração pelo ID, assinatura incorreta, expiração e rejeição do token antigo baseado em e-mail |
| `SecurityFilterTest` | 2 | carregamento do perfil atual do banco e bloqueio de usuário inativo |
| `SenhaDemonstracaoTest` | 1 | compatibilidade entre a senha `password` e o hash BCrypt das migrations |
| **Total** | **11** | **11 aprovados, sem falhas, erros ou testes ignorados** |

## 4. Verificações do script da API

O arquivo [`scripts/testar-api.ps1`](../../../scripts/testar-api.ps1) executou e aprovou:

- login dos perfis Administrador, Leitor e Atendente;
- acesso do Administrador ao painel administrativo;
- rejeição de senha incorreta com HTTP 401;
- rejeição de acesso sem token com HTTP 401;
- rejeição do Leitor na listagem administrativa com HTTP 403;
- acesso do Administrador à área do Atendente;
- rejeição do Atendente na área administrativa com HTTP 403.

Para repetir os fluxos HTTP, utilizar a [coleção Postman](../../Postman/sprint-1.postman_collection.json). As asserções exibidas em **Test Results** no Postman são verificações por requisição e não se somam aos 11 testes unitários.

## 5. Resultado geral

- **Testes unitários:** 11 aprovados, sem falhas ou erros;
- **Verificações do script HTTP:** 9 aprovadas;
- **Capturas manuais no Postman:** 13;
- **Arquivos de evidência referenciados:** 17, incluindo infraestrutura, banco, suíte unitária, script HTTP e capturas do Postman;
- **Fluxos registrados:** autenticação, autorização, gerenciamento de usuários, cadastro de leitores e rejeição de documento duplicado;
- **Falhas observadas nas verificações apresentadas:** nenhuma;
- **Defeitos registrados a partir dos resultados apresentados:** nenhum;


As capturas utilizam somente credenciais locais de demonstração. O JWT exibido na evidência de login era temporário, válido por duas horas, e não corresponde a um ambiente publicado.
