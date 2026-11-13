# Relatório de Entrega — Sprint 3 — Backend H11 e H13

**Responsável pelo incremento:** Raniery Chiarelli — RA 2840482321007

**Data:** 16/10/2026

**Branch de trabalho:** `feature/backend-sprint-3`

**Base:** `main`, commit `b5ccd0f7f22672d02ddd371d70e3c0eae975b967`

## 1. Incremento implementado

| História | Entrega de backend |
|---|---|
| H11 — Multas | Consulta individual e paginada, filtros por situação/leitor/empréstimo e baixa de multa pendente com data do servidor. A operação preserva os valores históricos e impede baixa repetida ou simultânea. |
| H13 — Configurações | Consulta e edição administrativa do prazo de empréstimo, limite de empréstimos, diária, teto da multa e prazo de retirada de reserva, com validação dos limites do schema. |

A implementação reutiliza as tabelas `multa` e `configuracao_biblioteca` e as migrations V1–V3 da base. A geração automática de multas continua no fluxo H8. As novas rotas utilizam a autorização por perfil já existente.

## 2. Contrato e regras

- Atendente ou administrador: `GET /atendente/multas`, `GET /atendente/multas/{id}` e `POST /atendente/multas/{id}/baixa`.
- Administrador: `GET /admin/configuracao` e `PUT /admin/configuracao`.
- Leitor: as rotas operacionais de multas e de configuração retornam HTTP 403.
- Baixa: somente multa pendente e sem pagamento registrado pode passar para quitada; a API define `pagaEm`.
- Configuração: PUT recebe os cinco campos completos; cada atualização grava um conjunto atômico de regras.
- Alterar regras preserva a data prevista dos empréstimos já abertos e os valores das multas já geradas. A diária e o teto de uma nova multa são os vigentes na devolução.

O [README](../../../README.md#contrato-do-backend-h11-e-h13) contém os corpos, filtros e respostas HTTP.

## 3. Validação técnica

Em 16/10/2026, Raniery Chiarelli executou localmente a suíte automatizada com Maven Wrapper e PostgreSQL em banco descartável:

| Verificação | Resultado |
|---|---|
| Suíte completa — `clean verify` | 149 testes aprovados; 0 falhas, 0 erros e 0 ignorados |
| Serviço, segurança e MVC | 67 testes aprovados |
| Integração com PostgreSQL | 82 testes aprovados: 47 existentes e 35 novos de H11/H13 |
| Flyway | V1, V2 e V3 aplicadas no banco descartável |
| Coleção Postman | 13 requisições preparadas para reprodução local |

Os resultados e cenários da validação automatizada estão nas [evidências de teste](sprint-3-evidencias-teste.md).

## 4. Alcance do incremento

Este relatório documenta o backend H11/H13 sobre a base H1–H8 e suas regressões. O prazo de retirada configurável foi verificado com fila preparada na fixture. O aceite integrado das telas e dos endpoints H9/H10 pertence à conferência conjunta do produto.

## 5. Registros individuais

- [Contribuição de Raniery Chiarelli](sprint-3-contribuicao-raniery.md)
- [Retrospectiva individual](sprint-3-retrospectiva.md)

## 6. Uso de IA generativa

Utilizei apoio substancial de IA generativa para agilizar a implementação, a elaboração dos testes e a documentação. Revisei o código, os documentos e os resultados obtidos, mantendo a responsabilidade pelas decisões técnicas e pela entrega.
