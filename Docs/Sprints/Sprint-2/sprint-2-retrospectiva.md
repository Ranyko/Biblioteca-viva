# Registro de Retrospectiva — Sprint 2 — Biblioteca Viva

**Autor do registro:** Raniery Chiarelli — RA 2840482321007

**Atualização:** 02/10/2026

**Estado:** reflexão individual após a execução local dos testes de backend H4–H8.

Este registro reúne observações sobre minha contribuição e os resultados obtidos. Seu preenchimento é individual e não corresponde a uma reunião coletiva de retrospectiva.

## 1. Continuidade da Sprint 1

| Ação da minha parte | Resultado |
|---|---|
| Organizar documentos e evidências da Sprint 1 | Fechamento registrado no commit `12787cd` |
| Levar H4/H5 à Sprint 2 junto com H6–H8 | Histórias reunidas no incremento de backend |
| Separar a Sprint 2 em branch própria | Branch local `feature/backend-sprint-2` criada |
| Validar o incremento no meu ambiente | 111 testes automatizados aprovados e 13 cenários Postman com o retorno esperado |

## 2. O que funcionou

- Reutilizar schema, autenticação e perfis manteve a continuidade do backend.
- Separar livro e exemplar permitiu controlar as unidades físicas e preservar suas movimentações.
- Executar os testes com PostgreSQL descartável verificou persistência, migrations, transações e concorrência em uma base reproduzível.
- A coleção Postman encadeou os IDs de autor, categoria, livro, exemplar e empréstimo, facilitando a demonstração do fluxo completo.
- Registrar os testes positivos e negativos mostrou tanto as operações permitidas quanto os bloqueios esperados.
- Reunir capturas e resultados no documento de evidências facilitou a conferência da entrega.

## 3. Aprendizados e melhorias de processo

O resultado do Maven precisa ser acompanhado pelos totais de falhas, erros e casos ignorados. Nesta execução, todos os 111 casos foram executados e aprovados, incluindo os 44 de integração com PostgreSQL.

A legibilidade do método, endpoint, dados relevantes e resposta facilita a conferência das capturas. A padronização dos nomes ajuda a relacionar cada imagem ao cenário executado e ao resultado esperado.

Os testes com dados preparados de reservas verificam as regras de circulação e fila. Essa execução delimitou os resultados do backend H4–H8; o fluxo integrado com os endpoints H9/H10 e as telas não foi abrangido por essas evidências.

## 4. Resultado da minha execução

Em 02/10/2026, a suíte executada no Windows 11, com Java 21.0.4 e PostgreSQL 15.19, terminou com **111 testes aprovados, zero falhas, zero erros e zero ignorados**. Os 13 cenários do Postman tiveram os retornos esperados. Os detalhes estão em [sprint-2-evidencias-teste.md](sprint-2-evidencias-teste.md).

## 5. Uso de IA generativa

Houve apoio substancial do Codex no código, testes, revisão e documentos. Raniery realizou a execução local dos testes, revisão do código/docs e a produção das capturas de 02/10/2026.

