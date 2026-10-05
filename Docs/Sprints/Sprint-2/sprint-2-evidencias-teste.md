# Evidências de Teste — Sprint 2 — Biblioteca Viva

**Responsável pelo incremento e execução local:** Raniery Chiarelli — RA 2840482321007

**Escopo:** backend H4–H8, com regressões da Sprint 1

**Data da execução:** 02/10/2026 — horário de São Paulo

**Branch:** `feature/backend-sprint-2`

**Implementação avaliada:** backend H4–H8, com a fundação H1–H3 da Sprint 1, na execução local de 02/10/2026.

## 1. Resultado da execução local

| Verificação | Resultado |
|---|---|
| Suíte Maven/JUnit | **111 testes aprovados, 0 falhas, 0 erros e 0 ignorados** |
| Testes de serviço, segurança e MVC | 67 aprovados |
| Integração com PostgreSQL | 44 aprovados: 22 em `CatalogoPostgresTest` e 22 em `CirculacaoPostgresTest` |
| Flyway no banco de testes | Três migrations validadas; aplicação de V1, V2 e V3 registrada nos logs |
| Postman | **13 cenários com os retornos esperados**, incluindo respostas de validação e conflito |

Ambiente registrado nos relatórios: **Windows 11, Java 21.0.4 (Red Hat) e PostgreSQL 15.19**. O Maven Wrapper do projeto está configurado para Maven 3.9.6. O banco de integração foi `biblioteca_sprint2_test`, acessado na porta `55432`. A API demonstrada no Postman usa a configuração local da coleção, `http://localhost:8080`.

O resumo do terminal e os 11 relatórios XML em `target/surefire-reports/TEST-*.xml` confirmam os totais. Os logs de integração registram a execução por volta das 14h25.

Comando de reprodução, com o banco descartável configurado:

```powershell
.\mvnw.cmd clean verify
```

As variáveis e os comandos para reproduzir os testes estão no [README](../../../README.md#testes-de-integração-com-postgresql). Os relatórios em `target/` são gerados pelo Maven; o registro versionado dos resultados fica neste documento e nas capturas.

## 2. Suíte automatizada executada

| Classe ou grupo | Executados | Aprovados | Falhas/erros/ignorados |
|---|---:|---:|---|
| Testes originais de autenticação/segurança da Sprint 1 | 11 | 11 | 0 / 0 / 0 |
| `CatalogoCadastroServiceTest` | 22 | 22 | 0 / 0 / 0 |
| `CatalogoCadastroControllerTest` | 5 | 5 | 0 / 0 / 0 |
| `AcervoConsultaServiceTest` | 5 | 5 | 0 / 0 / 0 |
| `CirculacaoServiceTest` | 20 | 20 | 0 / 0 / 0 |
| `CirculacaoControllerTest` | 4 | 4 | 0 / 0 / 0 |
| `CatalogoPostgresTest` | 22 | 22 | 0 / 0 / 0 |
| `CirculacaoPostgresTest` | 22 | 22 | 0 / 0 / 0 |
| **Total** | **111** | **111** | **0 / 0 / 0** |

`CatalogoPostgresTest` inclui quatro regressões da Sprint 1: edição de usuário, bloqueio do token após inativação, cadastro de leitor e rejeição de documento duplicado.

Os casos executados cobrem cadastro e edição com preservação de vínculos, ISBN/tombo duplicados, permissões reais, filtros e disponibilidade, leitor inativo/limite, empréstimo e devolução, cálculo/teto/valor zero de multa, persistência, fila preparada, rollback e concorrência. As regras de teto e multa zero também têm testes de serviço; a persistência de multa por atraso é exercitada com PostgreSQL.

## 3. Capturas da execução

Coleção utilizada: [sprint-2-h4-h8.postman_collection.json](../../Postman/sprint-2-h4-h8.postman_collection.json).

O registro das capturas utiliza a pasta `Docs/Evidencias/Sprint-2/`. Todos os cenários HTTP tiveram o retorno esperado.

| Caso | Resultado observado | Arquivo de evidência |
|---|---|---|
| S2-PREP01 — login administrativo | 200; usuário 1, perfil administrador e token emitido | [01-login-admin-200.png](../../Evidencias/Sprint-2/01-login-admin-200.png) |
| H4-01 — criar autor | 201; autor ID 5 | [02-autor-criado-201.png](../../Evidencias/Sprint-2/02-autor-criado-201.png) |
| H4-02 — criar categoria | 201; categoria ID 6 | [03-categoria-criada-201.png](../../Evidencias/Sprint-2/03-categoria-criada-201.png) |
| H4-03 — criar livro com vínculos | 201; livro ID 5, autor 5 e categoria 6 | [04-livro-criado-201.png](../../Evidencias/Sprint-2/04-livro-criado-201.png) |
| H4-04 — repetir ISBN | 409; ISBN já cadastrado | [05-isbn-duplicado-409.png](../../Evidencias/Sprint-2/05-isbn-duplicado-409.png) |
| CT04 — livro sem ISBN | 400; campo ISBN obrigatório | [06-livro-sem-isbn-400.png](../../Evidencias/Sprint-2/06-livro-sem-isbn-400.png) |
| H5-01 — criar exemplar | 201; exemplar ID 7, livro 5, situação disponível | [07-exemplar-criado-201.png](../../Evidencias/Sprint-2/07-exemplar-criado-201.png) |
| H5-02 — repetir tombo | 409; tombo já cadastrado | [08-tombo-duplicado-409.png](../../Evidencias/Sprint-2/08-tombo-duplicado-409.png) |
| H6-01 — pesquisar obra | 200; livro 5 e um exemplar disponível | [09-pesquisa-acervo-200.png](../../Evidencias/Sprint-2/09-pesquisa-acervo-200.png) |
| H7-01 — emprestar exemplar | 201; empréstimo 3, leitor 2, exemplar 7 e prazo até 16/10/2026 | [10-emprestimo-criado-201.png](../../Evidencias/Sprint-2/10-emprestimo-criado-201.png) |
| H5-03 — inativar exemplar emprestado | 409; movimentação ativa protege a situação | [11-inativacao-emprestado-409.png](../../Evidencias/Sprint-2/11-inativacao-emprestado-409.png) |
| H8-01 — devolver no prazo | 200; atraso 0, multa 0,00 e exemplar disponível | [12-devolucao-no-prazo-200.png](../../Evidencias/Sprint-2/12-devolucao-no-prazo-200.png) |
| H8-02 — repetir devolução | 409; empréstimo já devolvido | [13-devolucao-repetida-409.png](../../Evidencias/Sprint-2/13-devolucao-repetida-409.png) |
| S2-AUTO01 — suíte automatizada | 111 executados; zero falhas, erros e ignorados | [14-maven-clean-verify-111-testes.png](../../Evidencias/Sprint-2/14-maven-clean-verify-111-testes.png) |

O fluxo usa o mesmo livro 5, exemplar 7 e empréstimo 3. A retirada foi registrada em 02/10/2026, com prazo automático de 14 dias. A devolução no mesmo dia deixou o exemplar disponível e não gerou multa. As respostas 400 e 409 acima são resultados esperados dos testes negativos.

## 4. Alcance da validação

Os resultados comprovam a aprovação dos cenários automatizados e do fluxo local demonstrado de H4–H8. Os testes de reservas usam dados preparados por fixture e verificam a interação da circulação com a fila. A integração completa com os endpoints H9/H10 e com as telas está fora do alcance desta execução.

Esta execução local foi realizada por Raniery. O apoio de IA na implementação e documentação está descrito no [relatório individual](sprint-2-contribuicao-raniery.md).
