# Registro de Retrospectiva — Sprint 1 — Biblioteca Viva

**Estado:** retrospectiva registrada por Raniery Chiarelli. Não houve reunião coletiva de retrospectiva nesta sprint; o documento reúne sua reflexão sobre o trabalho realizado e propostas de melhoria.

**Autor do registro:** Raniery Chiarelli — RA 2840482321007

**Atualização do documento:** 02/10/2026

As observações refletem a análise individual do trabalho realizado e a organização informada por Raniery. As ações propostas para a equipe não são decisões aprovadas em uma reunião.

## 1. Ações da retrospectiva anterior — foram aplicadas?

Não se aplica: esta foi a primeira sprint de desenvolvimento.

## 2. O que funcionou bem

- O DER e o schema produzidos nas etapas anteriores forneceram uma base consistente para as entidades e migrations.
- A adaptação do Auth-Guard acelerou a implementação de JWT, BCrypt e controle de acesso por perfil.
- Docker Compose e Flyway tornaram a criação do banco reproduzível e verificável.
- A branch preservou a `main` enquanto a fundação do backend era desenvolvida e testada.
- Os testes automatizados e o script da API facilitaram a verificação dos códigos HTTP 200, 401 e 403.
- As evidências foram organizadas com nomes padronizados e vinculadas aos resultados executados.

## 3. O que não funcionou

- O escopo inicial da Sprint 1 ficou maior do que a capacidade disponível: livros e exemplares não foram implementados.
- Meu trabalho nesta sprint concentrou-se no backend; os fluxos integrados com as telas não foram verificados nesta entrega.
- Os documentos precisavam refletir os resultados dos fluxos testados em 02/10.
- Organizar o versionamento e os documentos da minha entrega exigiu ajustes para manter os registros coerentes com os testes realizados.

## 4. Ações para a próxima sprint

| Ação | Responsável | Como verificar no próximo incremento |
|---|---|---|
| Integrar login, armazenamento do token e rotas por perfil no frontend React | Isaac | Login consumindo `/auth/login`, rota protegida e evidência da integração |
| Implementar na Sprint 2 o backend H4/H5, junto com H6–H8 | Raniery | Endpoints, validações e testes de acervo, pesquisa, empréstimo e devolução na branch da Sprint 2 |
| Implementar as histórias H9 e H10 | Vinicius | Reservas e fila funcionando, com regras alinhadas ao backlog e evidências de execução |
| Alinhar devoluções com a fila de reservas | Raniery e Vinicius | Contrato e estados definidos; devolução com fila encaminha o exemplar ao leitor correto |
| Executar testes da própria implementação e participar dos testes integrados | Toda a equipe | Resultados identificam executor real, versão, data e evidência; registram os fluxos efetivamente executados |
| Conferir código e documentos de outro integrante antes da integração | Toda a equipe | Apontamentos e resposta registrados no PR ou na conversa do grupo |
| Consolidar a organização dos documentos e o versionamento da entrega atual | Raniery | Documentos coerentes com os resultados e alterações commitadas/enviadas |
