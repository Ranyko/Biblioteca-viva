# Biblioteca Viva

Sistema web de gerenciamento de biblioteca comunitária, desenvolvido como projeto acadêmico da disciplina de Laboratório de Engenharia de Software. O sistema reúne catálogo, usuários, empréstimos, devoluções, reservas e recursos complementares de apoio à biblioteca.

**Aplicação:** https://biblioteca-viva.duckdns.org/

**Equipe**
- Raniery Chiarelli — RA 2840482321007
- Vinicius Rocha — RA 2840482523051
- Isaac Leonardo da Silva — RA 2840482421016
- Pedro Netto de Sousa Lima — RA 2840482323022
- Quinto integrante — nome completo e RA a confirmar

**Instituição:** Fatec Ribeirão Preto  
**Curso:** Análise e Desenvolvimento de Sistemas  
**Disciplina:** Laboratório de Engenharia de Software

## Stack

- **Frontend:** React 19, TypeScript, React Router e Vite 8.
- **Backend:** Java 21, Spring Boot 4, Spring Security, Spring Data JPA e Spring Web MVC.
- **Banco de dados:** PostgreSQL 15.
- **Persistência e migrações:** Hibernate/JPA e Flyway.
- **Autenticação:** JWT.
- **Infraestrutura local:** Docker Compose.
- **Build e testes do backend:** Maven Wrapper.
- **Qualidade e integração contínua:** GitHub Actions.

## Como rodar localmente

### Pré-requisitos

- Git.
- JDK 21.
- Node.js e npm em versões compatíveis com Vite 8.
- Docker Desktop com Docker Compose.
- Portas locais 15432, 8080 e 5173 disponíveis.

### 1. Obter o projeto

```bash
git clone https://github.com/Ranyko/Biblioteca-viva.git
cd Biblioteca-viva
```

### 2. Configurar as variáveis de ambiente

Na raiz do projeto, crie o arquivo `.env` a partir do exemplo:

```bash
cp .env.example .env
```

No Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

O arquivo raiz é utilizado pelo Docker Compose para configurar o PostgreSQL. As variáveis abaixo também são lidas pela aplicação Spring Boot quando estiverem definidas no ambiente do processo que inicia o backend.

| Variável | Finalidade | Valor padrão de desenvolvimento |
|---|---|---|
| `DB_USER` | Usuário do PostgreSQL | `postgres` |
| `DB_PASSWORD` | Senha do PostgreSQL | `postgres` |
| `DB_NAME` | Nome do banco | `biblioteca_viva` |
| `DB_URL` | URL JDBC utilizada pelo backend | `jdbc:postgresql://localhost:15432/biblioteca_viva` |
| `JWT_SECRET` | Segredo de assinatura dos tokens JWT | Definido para uso local em `application.properties`; substitua em ambientes reais |
| `CORS_ALLOWED_ORIGINS` | Origem permitida para chamadas do frontend | `http://localhost:5173` |

**Importante:** o Docker Compose lê o arquivo `.env` automaticamente. O processo iniciado pelo Maven não importa esse arquivo por conta própria. Se alterar as configurações de conexão no `.env`, defina as variáveis correspondentes também no terminal ou na configuração de execução da IDE usada para iniciar o backend. Não compartilhe nem versione arquivos `.env` com credenciais reais.

### 3. Iniciar o banco de dados

Na raiz do repositório:

```bash
docker compose up -d
docker compose ps
```

O PostgreSQL 15 será disponibilizado na porta 15432 do computador e utilizará o volume persistente `biblioteca-viva-data`.

### 4. Iniciar o backend

Em macOS ou Linux:

```bash
./mvnw spring-boot:run
```

No Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

O backend ficará disponível em:

```
http://localhost:8080
```

As migrações do Flyway são executadas automaticamente na inicialização da aplicação. Os scripts estão em `src/main/resources/db/migration`; não é necessário executar o SQL manualmente.

### 5. Iniciar o frontend

Em outro terminal:

```bash
cd frontend
```

Crie o arquivo de ambiente do frontend:

```bash
cp .env.example .env
```

No Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

Instale as dependências e inicie o servidor de desenvolvimento:

```bash
npm ci
npm run dev
```

O frontend ficará disponível em:

```
http://localhost:5173
```

O arquivo `frontend/.env.example` define `VITE_API_URL=http://localhost:8080`, endereço local da API.

### 6. Usuários para demonstração

As migrações iniciais incluem usuários de demonstração. Todos utilizam a senha `password`.

| Perfil | E-mail |
|---|---|
| Administrador | `admin@biblioteca.com` |
| Atendente | `carlos@biblioteca.com` |
| Atendente | `ana@biblioteca.com` |
| Leitor | `joao@biblioteca.com` |
| Leitor | `maria@biblioteca.com` |

Essas credenciais são destinadas exclusivamente ao desenvolvimento e à demonstração acadêmica. Não devem ser utilizadas em produção.

## Estrutura do repositório

```text
Biblioteca-viva/
├── .github/
│   └── workflows/             # Integração contínua
├── Docs/                      # Documentação das entregas e sprints
├── frontend/                  # Aplicação React + TypeScript
├── src/
│   └── main/
│       ├── java/              # Código-fonte do backend Spring Boot
│       └── resources/
│           └── db/migration/  # Migrações SQL do Flyway
├── .env.example               # Exemplo de configuração do banco/backend
├── docker-compose.yml         # Serviço PostgreSQL para desenvolvimento
├── mvnw                       # Maven Wrapper (macOS/Linux)
├── mvnw.cmd                   # Maven Wrapper (Windows)
├── pom.xml                    # Configuração Maven do backend
└── LICENSE                    # Licença MIT
```

## Convenções da equipe

- **Branches:** as alterações são desenvolvidas em branches específicas e integradas à branch principal por Pull Request. Foram utilizadas branches com prefixos como `feature/`, `integracao/` e `docs/`, conforme o tipo de trabalho.
- **Commits:** as mensagens utilizam prefixos descritivos, como `feat:`, `fix:` e `docs:`, para indicar a natureza da alteração.
- **Pull Requests:** as alterações devem ser apresentadas em PR direcionado à branch principal, com descrição do objetivo e revisão antes da integração.
- **Organização:** documentação de entregas, evidências e relatórios de sprint é mantida no diretório `Docs/`.

## Testes

### Backend

Para executar os testes automatizados do backend:

Em macOS ou Linux:

```bash
./mvnw verify
```

No Windows PowerShell:

```powershell
.\mvnw.cmd verify
```

A suíte de testes é executada também pelo GitHub Actions em pushes e Pull Requests, utilizando Java 21 e PostgreSQL 15.

**Resultado registrado em 16/11/2026:** 277 testes executados, com 277 aprovações, zero falhas, zero erros e zero testes ignorados. A execução foi realizada pelo workflow de backend com o comando `./mvnw -B -ntp verify`.

A contagem refere-se à suíte automatizada do backend. A cobertura percentual de linhas e branches não foi medida nesse relatório, e não representa uma automação completa de navegação e interação da interface frontend.

## Licença / Uso acadêmico

Este projeto é distribuído sob a licença **MIT**. Consulte o arquivo [LICENSE](LICENSE) para obter o texto integral da licença.

O Biblioteca Viva foi desenvolvido para fins acadêmicos na disciplina de Laboratório de Engenharia de Software da Fatec Ribeirão Preto. As credenciais e os dados de demonstração incluídos nas migrações são destinados apenas a ambientes locais e apresentações do projeto.
