# Registro de Retrospectiva Individual — Sprint 3 — Backend H11/H13

**Responsável pelo registro:** Raniery Chiarelli — RA 2840482321007

**Data:** 15/10/2026

**Estado:** registro técnico individual sobre o incremento H11/H13.

## 1. Continuidade do trabalho

A base H1–H8 já integrada à `main` permitiu implementar multas e configurações sobre o schema e a autenticação existentes. A consulta/baixa da multa complementa sua geração na devolução, e as regras administrativas passam a ser editáveis por API.

## 2. O que funcionou

- Reutilizar `multa` e `configuracao_biblioteca` evitou duplicar registros e regras de negócio.
- Definir os contratos de autorização manteve separadas as operações de administrador e atendente.
- Testar a baixa simultânea verificou que apenas uma solicitação registra o pagamento.
- Executar a suíte completa com PostgreSQL confirmou as regressões das sprints anteriores junto aos novos casos.
- Verificar o efeito da configuração na circulação tornou explícita a preservação dos prazos e valores históricos.

## 3. Aprendizados técnicos

A alteração das regras deve afetar as operações posteriores sem modificar os registros históricos. A baixa precisa condicionar a gravação ao estado atual da multa; uma consulta anterior isolada não impede que duas solicitações tentem pagar o mesmo registro.

Os testes por perfil e com banco real complementam os testes isolados. Um resultado de compilação, sozinho, não demonstra autorização, persistência ou concorrência.

## 4. Melhoria de processo

O contrato da API e a coleção HTTP facilitam a conferência entre os integrantes. Distinguir os testes automatizados das verificações manuais torna os registros de validação mais claros.
