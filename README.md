# Biblioteca Viva

Sistema web para apoiar o controle de uma biblioteca comunitária, permitindo organizar o acervo e controlar leitores, empréstimos, devoluções, reservas, multas e indicadores de utilização.

> O projeto encontra-se na etapa de modelagem e preparação. A implementação do frontend e do backend será iniciada na Sprint 1.

**Deploy:** ainda não publicado — previsto para as próximas sprints.  
**Protótipo:** [INSERIR LINK DO FIGMA OU PENPOT]  

## Equipe

- Raniery Chiarelli — RA 2840482321007
- Vinicius Rocha — RA 2840482523051
- Isaac Leonardo da Silva — RA 2840482421016

**Disciplina:** Laboratório de Engenharia de Software  
**Curso:** Análise e Desenvolvimento de Sistemas — Fatec Ribeirão Preto  
**Semestre:** 2026/2  
**Trilha:** B  
**Tema:** Banco de temas nº 9 — Controle de biblioteca comunitária  

## Objetivo

O Biblioteca Viva busca substituir controles manuais ou descentralizados utilizados por bibliotecas comunitárias. O sistema permitirá acompanhar a situação dos exemplares, organizar filas de reserva, registrar empréstimos e devoluções, calcular multas por atraso e consultar indicadores de utilização do acervo.

## Funcionalidades planejadas

- autenticação com os perfis Leitor, Atendente e Administrador;
- cadastro e gerenciamento de usuários e leitores;
- cadastro de livros, autores, categorias e exemplares;
- pesquisa do acervo por título, autor ou categoria;
- registro de empréstimos e devoluções;
- fila de reservas;
- cálculo automático de multas;
- configuração das regras da biblioteca;
- área do leitor;
- painel com indicadores e relatórios.

O detalhamento das funcionalidades, prioridades e critérios de aceite está disponível no backlog da E2.

## Stack planejada

| Camada | Tecnologia |
|---|---|
| Frontend | React 19 + Vite 8 |
| Backend | Java 21 + Spring Boot 4.1 |
| Banco de dados | PostgreSQL 15+ |
| Autenticação | Spring Security + JWT |
| Versionamento | Git + GitHub |
| Deploy | Vercel para frontend; Render para backend; Neon para o banco de dados|


## Estado atual

Até o momento foram produzidos:

- Documento de Visão — E1;
- Backlog Priorizado e Termo de Aceite — E2;
- Diagrama UML, DER e script DDL — E3;
- configuração inicial do repositório, plano de testes e roteiro do protótipo — E4.

A aplicação ainda não possui frontend ou backend executável. Atualmente, é possível executar e validar o banco de dados PostgreSQL usando o arquivo `db/schema.sql`.

## Estrutura atual do repositório

```text
biblioteca-viva/
├── db/
│   └── schema.sql
├── docs/
│   ├── documento-de-visao.md
│   ├── backlog.md
│   ├── termo-de-aceite.md
│   ├── uml.md
│   ├── der.md
│   ├── plano-de-testes.md
│   └── prototipo.md
├── LICENSE
└── README.md
```


## Como preparar o projeto

### Pré-requisitos

- Git 2.5 ou superior;
- Docker Desktop com Docker Compose;
- um terminal, como PowerShell, Prompt de Comando, Bash ou Terminal do macOS.

### 1. Clonar o repositório

Substitua o endereço abaixo pela URL oficial do GitHub:

```bash
git clone https://github.com/Ranyko/Biblioteca-viva
cd biblioteca-viva
```

### 2. Criar o volume do banco

O volume mantém os dados mesmo quando o container é desligado ou removido:

```bash
docker volume create biblioteca-viva-data
```

### 3. Criar o PostgreSQL no Docker

Execute o comando abaixo em uma única linha:

```bash
docker run --name biblioteca-viva-db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=biblioteca_viva -p 5432:5432 -v biblioteca-viva-data:/var/lib/postgresql/data -d postgres:15
```

Confira se o container está funcionando:

```bash
docker ps
```

Se o container já tiver sido criado anteriormente e estiver parado, não execute novamente o `docker run`. Apenas inicie-o:

```bash
docker start biblioteca-viva-db
```

### 4. Executar o schema

Copie o arquivo SQL para dentro do container:

```bash
docker cp db/schema.sql biblioteca-db:/tmp/schema.sql
```

Execute o script no banco `biblioteca_viva`:

```bash
docker exec biblioteca-viva-db psql -U postgres -d biblioteca_viva -f /tmp/schema.sql
```

O script cria as tabelas, relacionamentos, constraints, índices e dados iniciais utilizados pelo projeto.


### 5. Conferir as tabelas

```bash
docker exec -it biblioteca-viva-db psql -U postgres -d biblioteca_viva -c "\dt"
```

O resultado deve listar 12 tabelas, incluindo `usuario`, `leitor`, `livro`, `exemplar`, `emprestimo`, `reserva` e `multa`.

Para consultar os usuários de exemplo:

```bash
docker exec -it biblioteca-viva-db psql -U postgres -d biblioteca_viva -c "SELECT id, nome, email, perfil FROM usuario ORDER BY id;"
```

### 6. Parar e iniciar o banco

Para parar o container sem apagar os dados:

```bash
docker stop biblioteca-viva-db
```

Para iniciá-lo novamente:

```bash
docker start biblioteca-viva-db
```

### Recriar o banco do zero

Os comandos abaixo removem o container e apagam permanentemente o volume com os dados locais. Use somente quando quiser reiniciar completamente o banco:

```bash
docker rm -f biblioteca-viva-db
docker volume rm biblioteca-viva-data
```

Depois, repita os passos de criação do volume, criação do container e execução do schema.

## Execução da aplicação

O frontend e o backend ainda não foram implementados. Os comandos para instalar dependências, executar a API, iniciar a interface e rodar testes automatizados serão adicionados durante a Sprint 1.

## Convenções da equipe

- Branches de funcionalidade: `feature/nome-da-funcionalidade`;
- correções: `fix/nome-da-correcao`;
- documentação: `docs/nome-da-alteracao`;
- commits seguindo Conventional Commits, como `feat:`, `fix:`, `docs:`, `test:` e `chore:`;


## Testes

A estratégia inicial, os critérios que bloqueiam merge e os casos planejados estão documentados em `docs/plano-de-testes.md`.

Os comandos dos testes automatizados serão adicionados quando os projetos de frontend e backend forem iniciados. Nesta etapa, o `schema.sql` pode ser verificado executando-o em um banco PostgreSQL vazio e consultando as tabelas criadas.

## Protótipo

O índice das telas, os perfis envolvidos e a relação com as histórias do backlog estão disponíveis em `docs/prototipo.md`.

O protótipo navegável será publicado no Figma ou Penpot com acesso de visualização liberado.

## Licença

Este projeto está licenciado sob a licença MIT. Consulte o arquivo [`LICENSE`](LICENSE) para mais informações.
