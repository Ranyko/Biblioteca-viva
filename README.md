# Biblioteca Viva

API do sistema de controle de biblioteca comunitária desenvolvido na disciplina de Laboratório de Engenharia de Software. Este incremento da Sprint 1 adapta a autenticação do projeto [Auth-Guard](https://github.com/Ranyko/Auth-Guard) ao domínio e ao banco de dados do Biblioteca Viva.

## Estado atual

O backend da Sprint 1 inclui autenticação, usuários e leitores. A validação local registrou PostgreSQL saudável, inicialização da API, 11 testes unitários aprovados e nove verificações HTTP aprovadas pelo script PowerShell. Os fluxos do Postman demonstram autenticação, autorização, criação/atualização/inativação de usuário, bloqueio do token anterior à inativação, cadastro de leitor e rejeição de documento duplicado.

Os resultados registrados em 23/09/2026 e 02/10/2026 foram reunidos nas [Evidências da Sprint 1](Docs/Sprints/Sprint-1/sprint-1-evidencias-teste.md), com uma tabela única e 17 arquivos de evidência.

Funcionalidades implementadas:

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

O Compose cria o banco `biblioteca_viva`. Na primeira execução da API, o Flyway aplica `V1__schema_inicial.sql` e `V2__corrige_senha_demonstracao.sql`, em `src/main/resources/db/migration`. A V2 corrige a senha de demonstração da versão anterior. Em banco já gerenciado por Flyway com V1 aplicada, somente a V2 será executada; senhas já alteradas são preservadas.

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

Os testes automatizados cobrem credenciais válidas e inválidas, usuário inativo, assinatura e expiração do JWT, identificação pelo ID, carregamento de autoridade pelo filtro e compatibilidade da senha de demonstração com BCrypt. O resultado registrado é de 11 testes aprovados, sem falhas ou erros.

As [Evidências da Sprint 1](Docs/Sprints/Sprint-1/sprint-1-evidencias-teste.md) registram os testes de autenticação e autorização, os retornos HTTP 200 na atualização e inativação de usuário, HTTP 401 com o token anterior à inativação, HTTP 201 no cadastro de leitor e HTTP 409 para documento duplicado.

Os fluxos HTTP demonstrados estão reunidos na [coleção Postman da Sprint 1](Docs/Postman/sprint-1.postman_collection.json), com os resultados registrados nas evidências.

## Documentos da E5

- [Relatório da Sprint 1](Docs/Sprints/Sprint-1/sprint-1-relatorio.md)
- [Backlog da E2](Docs/E1-E4/backlog-biblioteca-viva%28E2%29.md)
- [Evidências de teste](Docs/Sprints/Sprint-1/sprint-1-evidencias-teste.md)
- [Retrospectiva individual](Docs/Sprints/Sprint-1/sprint-1-retrospectiva.md)
- [Relatório individual de Raniery](Docs/Sprints/Sprint-1/sprint-1-contribuicao-raniery.md)

## Estrutura principal

```text
src/main/java/br/com/bibliotecaviva/
├── config/       # configuração do Spring Security e CORS
├── controller/   # endpoints HTTP
├── dto/          # dados de entrada e saída
├── exception/    # erros padronizados
├── model/        # entidades e perfis
├── repository/   # acesso ao PostgreSQL
├── security/     # filtro JWT
└── service/      # regras de autenticação, usuários e leitores
```

O arquivo `db/schema.sql` contém o DDL e o seed atualizado para um banco vazio. Na aplicação, use apenas as migrações em `src/main/resources/db/migration`. A V1 original foi preservada para manter seu checksum; a V2 corrige o hash sem recriar tabelas. Por isso, o SQL avulso representa o resultado de V1 + V2 e não é idêntico à V1.

O estado da entrega, as limitações e os itens replanejados estão registrados no [Relatório da Sprint 1](Docs/Sprints/Sprint-1/sprint-1-relatorio.md).

## Escopo além desta entrega de backend

- verificar os fluxos integrados das telas React com o backend;
- implementar livros, autores, categorias e exemplares;
- implementar empréstimos, devoluções, reservas e multas;
- adicionar testes de integração com PostgreSQL;
- preparar o deploy do frontend, API e banco.

## Equipe

- Raniery Chiarelli — RA 2840482321007
- Vinicius Rocha — RA 2840482523051
- Isaac Leonardo da Silva — RA 2840482421016

## Licença

Distribuído sob a licença MIT. Consulte `LICENSE`.
