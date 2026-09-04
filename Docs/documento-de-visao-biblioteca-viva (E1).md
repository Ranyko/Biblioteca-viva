# Documento de Visão — Biblioteca Viva

**Equipe:** Raniery Chiarelli (2840482321007) — Vinicius Rocha (2840482523051) — Isaac Leonardo da Silva (2840482421016)<br>
**Trilha:** B  
**Origem do problema:** Banco de temas nº 9 — Controle de biblioteca comunitária  
**Data:** 26/08/2026

## 1. Problema

Bibliotecas comunitárias costumam trabalhar com poucos recursos e podem controlar o acervo, os leitores e os empréstimos por meio de cadernos ou planilhas. Esse processo dificulta saber rapidamente quais exemplares estão disponíveis, quem está com cada item e quais devoluções estão atrasadas. Reservas também podem se perder ou ser atendidas fora da ordem, enquanto o cálculo manual de multas aumenta a chance de erros e retrabalho. Além disso, sem um histórico centralizado, torna-se difícil identificar os livros mais procurados, acompanhar o uso do acervo e oferecer sugestões de leitura adequadas aos interesses de cada leitor. 

Como não houve levantamento em uma biblioteca real, o projeto adotará como cenário de referência uma biblioteca comunitária com até 1.000 obras, 2.000 exemplares e 500 leitores cadastrados, realizando aproximadamente 100 empréstimos por semana e mantendo até 20 reservas simultâneas. Para validar os fluxos relacionados a atrasos, a massa de testes deverá conter pelo menos 100 empréstimos históricos, incluindo 15 devoluções atrasadas. Esses valores representam parâmetros de dimensionamento e validação do MVP, não dados obtidos por pesquisa de campo.

## 2. Público-alvo e perfis de usuário

| Perfil | Quem é | O que faz no sistema |
|---|---|---|
| Leitor | Pessoa cadastrada que utiliza a biblioteca | Consulta o acervo e a disponibilidade, realiza reservas, acompanha seus empréstimos, multas e sugestões de leitura |
| Atendente | Voluntário ou funcionário responsável pelo atendimento diário | Cadastra leitores, registra empréstimos e devoluções, consulta reservas e acompanha pendências |
| Administrador | Responsável pela gestão da biblioteca | Gerencia o acervo e os usuários, define regras de empréstimo e multa e consulta relatórios |

## 3. Visão da solução

O Biblioteca Viva será um sistema web para centralizar o controle do acervo e o atendimento de uma biblioteca comunitária. Cada livro poderá possuir um ou mais exemplares, permitindo que o sistema informe a disponibilidade real para empréstimo e organize reservas em fila. Empréstimos e devoluções serão registrados pelos atendentes, com identificação automática de atrasos e cálculo de multa segundo as regras definidas pela biblioteca. O sistema também utilizará o histórico de empréstimos e as categorias preferidas do leitor para apresentar sugestões simples de obras disponíveis.

## 4. Objetivos do MVP

- Centralizar em uma única base 100% dos leitores, obras, autores, categorias e exemplares utilizados na validação do MVP, impedindo duplicidades de e-mail, documento, ISBN e código de tombo.
- Permitir a consulta da disponibilidade de uma obra em até 3 segundos no ambiente de testes e bloquear 100% das tentativas de empréstimo de exemplares indisponíveis previstas nos casos de teste.
- Manter a ordem de solicitação em 100% dos cenários de reserva testados e alocar o próximo exemplar disponível ao primeiro leitor elegível da fila.
- Identificar corretamente todas as devoluções atrasadas dos casos de teste e calcular a multa pela fórmula `dias de atraso × valor diário configurado`, sem necessidade de cálculo manual.
- Apresentar no painel pelo menos quatro indicadores: total de empréstimos ativos, total de empréstimos atrasados, cinco obras mais emprestadas e movimentações agrupadas por categoria.
- Apresentar ao leitor até cinco sugestões de obras disponíveis pertencentes às categorias mais presentes em seu histórico de empréstimos.

## 5. Fora de escopo

- Aplicativo móvel nativo; o MVP será um sistema web responsivo.
- Pagamento on-line de multas; o sistema apenas calcula, registra e permite dar baixa no valor.
- Integração com catálogos externos, ISBN ou outras bibliotecas.
- Envio de avisos por WhatsApp, SMS ou e-mail.
- Recomendação por inteligência artificial; no MVP, as sugestões usarão categorias e histórico de empréstimos.
- Empréstimo de materiais digitais protegidos por direitos autorais.

## 6. Requisitos mínimos do §3 do Manual

| Requisito mínimo | Como este projeto cobre |
|---|---|
| Autenticação com 2+ perfis | Login com os perfis Leitor, Atendente e Administrador, cada um com permissões diferentes |
| 6+ entidades com relacionamento N:N | Usuário, Leitor, Livro, Exemplar, Autor, Categoria, Empréstimo, Reserva e Multa; Livro e Autor possuem relação N:N, assim como Livro e Categoria |
| Regra de negócio não trivial | Controle de disponibilidade, fila de reservas por ordem de solicitação e cálculo de multa por dias de atraso |
| Consulta agregada (relatório/dashboard) | Painel com obras mais emprestadas, empréstimos por categoria e totais de empréstimos ativos e atrasados, usando agrupamentos e junções entre três ou mais tabelas |
| Validações em interface e banco | Campos obrigatórios e mensagens na interface, além de restrições NOT NULL, UNIQUE, CHECK e chaves estrangeiras no banco |
| Deploy público por URL | Aplicação web e banco de dados serão publicados em serviços acessíveis por URL; a plataforma será definida posteriormente |
| Repositório Git com README | Código mantido em repositório Git compartilhado, com README contendo requisitos e instruções completas para executar o projeto |

## 7. Riscos identificados

| Risco | Impacto | Mitigação |
|---|---|---|
| Escopo crescer com funções secundárias | Atraso nas funcionalidades essenciais | Priorizar cadastro, empréstimo, devolução, reserva, multa e relatório; manter integrações e notificações fora do MVP |
| Regras de multa e prazo não terem sido definidas por uma biblioteca real | Implementação incompatível com a prática do usuário | Tornar prazo, limite de empréstimos e valor diário configuráveis pelo administrador e validar as regras com o professor como Cliente/validador |
| Equipe não dominar a stack escolhida | Atraso na configuração e na primeira sprint | Escolher tecnologias que pelo menos algum dos integrantes já tenha utilizado |
| Falta de dados reais para testes e relatórios | Demonstração pouco representativa | Criar uma massa de dados fictícia com livros, exemplares, leitores, empréstimos e reservas desde a preparação do projeto |
| Lógica de reserva ou disponibilidade apresentar inconsistências | Um exemplar ser reservado ou emprestado incorretamente | Definir as regras antes da implementação e criar testes específicos para empréstimo, devolução, fila e atraso |
