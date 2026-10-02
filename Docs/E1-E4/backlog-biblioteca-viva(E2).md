# Backlog Priorizado — Biblioteca Viva

**Equipe:** Raniery Chiarelli (2840482321007) — Vinicius Rocha (2840482523051) — Isaac Leonardo da Silva (2840482421016)  
**Trilha:** B  
**Origem:** Banco de temas nº 9 — Controle de biblioteca comunitária  
**Data:** 26/08/2026  


## Backlog

| # | História de usuário | Critérios de aceite | Prioridade (MoSCoW) | Estimativa | Sprint alvo |
|---|---|---|---|---:|---|
| 1 | Como usuário, quero entrar no sistema com e-mail e senha, para acessar somente as funções permitidas ao meu perfil. | E-mail deve estar cadastrado; senha incorreta deve gerar mensagem clara; o sistema deve diferenciar Leitor, Atendente e Administrador; usuário inativo não pode entrar. | Must | 5 | Sprint 1 |
| 2 | Como administrador, quero cadastrar e gerenciar usuários, para controlar quem utiliza o sistema. | E-mail deve ser único; nome, e-mail, perfil e situação são obrigatórios; deve ser possível ativar e inativar usuário; não deve ser possível excluir usuário com movimentações registradas. | Must | 5 | Sprint 1 |
| 3 | Como atendente, quero cadastrar leitores, para permitir que utilizem os serviços da biblioteca. | Documento e e-mail devem ser únicos; nome e contato são obrigatórios; leitor inicia ativo; sistema deve mostrar erros de validação. | Must | 3 | Sprint 1 |
| 4 | Como administrador, quero cadastrar livros com autores e categorias, para organizar o acervo. | Título e ISBN são obrigatórios; ISBN deve ser único; um livro pode ter vários autores e categorias; deve permitir edição sem perder o histórico. | Must | 5 | Sprint 1 |
| 5 | Como administrador, quero cadastrar exemplares de uma obra, para controlar cada unidade física separadamente. | Exemplar deve ter código único; deve estar vinculado a um livro; estado de conservação é obrigatório; status inicial deve ser “disponível”. | Must | 3 | Sprint 1 |
| 6 | Como leitor, quero pesquisar o acervo, para encontrar uma obra de meu interesse. | Deve permitir busca por título, autor ou categoria; deve mostrar quantidade de exemplares disponíveis; obras sem exemplares disponíveis continuam visíveis. | Must | 5 | Sprint 2 |
| 7 | Como atendente, quero registrar um empréstimo, para saber qual leitor está com cada exemplar. | Somente exemplar disponível pode ser emprestado; leitor deve estar ativo e dentro do limite de empréstimos; retirada e prazo de devolução devem ser registrados; exemplar passa para “emprestado”. | Must | 8 | Sprint 2 |
| 8 | Como atendente, quero registrar uma devolução, para atualizar a disponibilidade e identificar atrasos. | Data da devolução deve ser registrada; atraso deve ser calculado; se não houver reserva, exemplar volta para “disponível”; se houver fila, passa para “reservado”. | Must | 8 | Sprint 2 |
| 9 | Como leitor, quero reservar uma obra indisponível, para entrar na fila de espera. | Reserva só pode ser criada quando não houver exemplar disponível; leitor não pode reservar a mesma obra duas vezes; fila deve respeitar data e hora da solicitação; posição deve ser exibida. | Must | 8 | Sprint 2 |
| 10 | Como atendente, quero consultar a fila de reservas, para entregar o próximo exemplar à pessoa correta. | Deve mostrar a ordem da fila; somente o primeiro leitor pode retirar o exemplar liberado; reserva atendida ou cancelada deixa de ocupar posição. | Must | 5 | Sprint 2 |
| 11 | Como atendente, quero que a multa por atraso seja calculada automaticamente, para evitar erros no valor cobrado. | Multa deve considerar dias de atraso e valor diário configurado; devolução no prazo gera valor zero; valor e situação da multa devem ser registrados; atendente pode registrar a baixa. | Must | 8 | Sprint 3 |
| 12 | Como leitor, quero acompanhar meus empréstimos, reservas e multas, para saber minha situação na biblioteca. | Deve mostrar empréstimos ativos e prazos; reservas e posições na fila; multas pendentes e quitadas; leitor só pode visualizar seus próprios dados. | Should | 5 | Sprint 3 |
| 13 | Como administrador, quero configurar as regras da biblioteca, para adaptar o sistema ao funcionamento da instituição. | Deve permitir definir prazo padrão, limite de empréstimos, valor diário da multa e prazo para retirada de reserva; valores inválidos devem ser bloqueados. | Should | 5 | Sprint 3 |
| 14 | Como administrador, quero consultar um painel da biblioteca, para acompanhar a utilização do acervo. | Deve exibir totais de empréstimos ativos e atrasados; ranking de obras mais emprestadas; empréstimos agrupados por categoria; dados devem vir de consulta agregada com três ou mais tabelas. | Must | 8 | Sprint 3 |
| 15 | Como leitor, quero receber sugestões baseadas no meu histórico, para descobrir outras obras do acervo. | Deve considerar as categorias mais presentes no histórico; não deve sugerir a mesma obra já emprestada; deve priorizar obras com exemplar disponível. | Should | 5 | Sprint 3 |
| 16 | Como administrador, quero exportar o relatório de movimentações em CSV, para analisar ou compartilhar os dados. | Arquivo deve conter período, obra, leitor, retirada, prazo, devolução e situação; filtros aplicados devem ser respeitados. | Could | 3 | Sprint 4 |
| 17 | Como leitor, quero receber avisos por e-mail ou WhatsApp, para lembrar de prazos e reservas. | Fora do escopo neste semestre; o MVP exibirá os avisos somente dentro do sistema. | Won't | — | — |
| 18 | Como leitor, quero pagar multas pela internet, para regularizar minha situação sem ir à biblioteca. | Fora do escopo neste semestre; o sistema apenas calcula e registra a baixa da multa. | Won't | — | — |

#
