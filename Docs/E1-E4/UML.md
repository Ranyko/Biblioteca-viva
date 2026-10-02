# Diagramas UML — Biblioteca Viva

**Equipe:** Raniery Chiarelli (2840482321007) — Vinicius Rocha (2840482523051) — Isaac Leonardo da Silva (2840482421016)  
**Trilha:** B  
**Origem:** Banco de temas nº 9 — Controle de biblioteca comunitária  
**Data:** 04/09/2026

## 1. Diagrama de Casos de Uso

```mermaid
flowchart LR
  Leitor((Leitor))
  Atendente((Atendente))
  Admin((Administrador))

  Leitor --> UC1[Entrar no sistema]
  Atendente --> UC1
  Admin --> UC1

  Leitor --> UC2[Pesquisar acervo]
  Leitor --> UC3[Reservar obra indisponível]
  Leitor --> UC4[Acompanhar empréstimos, reservas e multas]
  Leitor --> UC5[Receber sugestões baseadas no histórico]

  Atendente --> UC6[Cadastrar leitores]
  Atendente --> UC7[Registrar empréstimo]
  Atendente --> UC8[Registrar devolução]
  Atendente --> UC9[Consultar fila de reservas]
  Atendente --> UC10[Registrar baixa de multa]
  UC8 -.include.-> UC17[Calcular multa por atraso]

  Admin --> UC11[Gerenciar usuários]
  Admin --> UC12[Cadastrar livros, autores e categorias]
  Admin --> UC13[Cadastrar exemplares]
  Admin --> UC14[Configurar regras da biblioteca]
  Admin --> UC15[Consultar painel da biblioteca]
  Admin --> UC16[Exportar relatório de movimentações]
```

## 2. Diagrama de Classes

```mermaid
classDiagram
  class Usuario {
    +id: long
    +nome: string
    +email: string
    +senhaHash: string
    +perfil: enum
    +ativo: bool
    +criadoEm: datetime
    +autenticar() bool
  }

  class Leitor {
    +id: long
    +documento: string
    +telefone: string
    +cadastradoEm: datetime
  }

  class Livro {
    +id: long
    +titulo: string
    +isbn: string
    +anoPublicacao: int
    +descricao: string
    +ativo: bool
    +consultarDisponibilidade() int
  }

  class Autor {
    +id: long
    +nome: string
  }

  class Categoria {
    +id: long
    +nome: string
  }

  class Exemplar {
    +id: long
    +codigoTombo: string
    +estadoConservacao: string
    +status: enum
    +cadastradoEm: datetime
    +podeSerEmprestado() bool
  }

  class Emprestimo {
    +id: long
    +dataRetirada: datetime
    +dataPrevistaDevolucao: date
    +dataDevolucao: datetime
    +estaAtrasado() bool
    +registrarDevolucao()
  }

  class Reserva {
    +id: long
    +dataSolicitacao: datetime
    +status: enum
    +dataDisponibilizacao: datetime
    +dataLimiteRetirada: datetime
    +calcularPosicaoFila() int
  }

  class Multa {
    +id: long
    +diasAtraso: int
    +valorDiarioAplicado: decimal
    +valorTotal: decimal
    +status: enum
    +geradaEm: datetime
    +pagaEm: datetime
    +calcularValor() decimal
    +registrarBaixa()
  }

  class ConfiguracaoBiblioteca {
    +id: int
    +prazoEmprestimoDias: int
    +limiteEmprestimos: int
    +valorMultaDia: decimal
    +prazoRetiradaReservaDias: int
    +atualizadoEm: datetime
  }

  Usuario "1" -- "0..1" Leitor : possui cadastro

  Livro "0..*" -- "0..*" Autor : possui
  Livro "0..*" -- "0..*" Categoria : pertence
  Livro "1" -- "0..*" Exemplar : possui

  Leitor "1" -- "0..*" Emprestimo : realiza
  Exemplar "1" -- "0..*" Emprestimo : movimenta
  Usuario "1" -- "0..*" Emprestimo : registra retirada
  Usuario "0..1" -- "0..*" Emprestimo : registra devolução

  Leitor "1" -- "0..*" Reserva : solicita
  Livro "1" -- "0..*" Reserva : recebe
  Exemplar "0..1" -- "0..*" Reserva : é alocado

  Emprestimo "1" -- "0..1" Multa : gera
```

## 3. Rastreabilidade — caso de uso → história do backlog

| Caso de uso | História(s) relacionada(s) (E2) |
|---|---|
| Entrar no sistema | #1 |
| Pesquisar acervo | #6 |
| Reservar obra indisponível | #9 |
| Acompanhar empréstimos, reservas e multas | #12 |
| Receber sugestões baseadas no histórico | #15 |
| Cadastrar leitores | #3 |
| Registrar empréstimo | #7 |
| Registrar devolução | #8 |
| Consultar fila de reservas | #10 |
| Registrar baixa de multa | #11 |
| Calcular multa por atraso | #11 |
| Gerenciar usuários | #2 |
| Cadastrar livros, autores e categorias | #4 |
| Cadastrar exemplares | #5 |
| Configurar regras da biblioteca | #13 |
| Consultar painel da biblioteca | #14 |
| Exportar relatório de movimentações | #16 |
