# Relatório Individual de Contribuição — Sprint 3 — Raniery Chiarelli

**Nome:** Raniery Chiarelli

**RA:** 2840482321007

**Frente:** backend H11/H13, testes e documentação

**Data:** 16/10/2026

## 1. Contribuição documentada

| Item | Resultado |
|---|---|
| H11 — Multas | Implementação da consulta individual/paginada, filtros e baixa transacional com data do servidor, preservação dos valores e proteção contra baixa repetida. |
| H13 — Configurações | Implementação da consulta e edição administrativa das regras, com validação dos tipos e limites do PostgreSQL. |
| Integração com H7/H8 | Verificação do efeito das regras nas operações posteriores e da preservação dos prazos e multas existentes. |
| Testes | Adição de 35 casos de integração de autorização, persistência, validação, histórico e concorrência. |
| Contrato e documentação | Atualização do README, preparação da coleção Postman e registros deste incremento. |

O trabalho usa a base `main` em `b5ccd0f` e a branch de trabalho `feature/backend-sprint-3`. Este registro se limita à contribuição H11/H13.

## 2. Validação registrada

Em 16/10/2026, executei localmente a suíte completa com `clean verify`: **149 testes aprovados**, sendo 82 de integração com PostgreSQL, sem falhas, erros ou casos ignorados. Os resultados estão nas [evidências de teste](sprint-3-evidencias-teste.md). A coleção Postman contém os cenários para reprodução local.

## 3. Decisões técnicas

- Reutilizar as tabelas e migrations existentes.
- Manter a geração de multas no fluxo de devolução e acrescentar sua consulta e baixa.
- Efetuar a baixa somente enquanto a multa estiver pendente, preservando o pagamento anterior.
- Atualizar as cinco regras em uma única operação, com validação antes da persistência.
- Registrar os valores históricos nas multas e preservar datas previstas já definidas.

## 4. Uso de IA generativa

Utilizei apoio substancial de IA generativa para agilizar a implementação, a elaboração dos testes e a documentação. Revisei o código, os documentos e os resultados obtidos, mantendo a responsabilidade pelas decisões técnicas e pela entrega.
