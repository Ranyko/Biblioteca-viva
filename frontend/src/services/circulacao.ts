import { api } from './api';

export interface Emprestimo {
  id: number;
  leitorId: number;
  exemplarId: number;
  atendenteRetiradaId: number;
  atendenteDevolucaoId: number | null;
  dataRetirada: string;
  dataPrevistaDevolucao: string;
  dataDevolucao: string | null;
}

export interface Devolucao {
  emprestimo: Emprestimo;
  diasAtraso: number;
  valorMulta: number;
  statusExemplar: 'disponivel' | 'reservado' | string;
  reservaDisponibilizadaId: number | null;
}

const TAMANHO = 100;

export const circulacaoService = {
  emprestar: (leitorId: number, exemplarId: number) =>
    api<Emprestimo>('/atendente/emprestimos', { method: 'POST', body: { leitorId, exemplarId } }),

  devolver: (emprestimoId: number) =>
    api<Devolucao>(`/atendente/emprestimos/${emprestimoId}/devolucao`, { method: 'POST' }),

  /** Todos os empréstimos ativos (o back devolve uma lista simples, então pagina até acabar). */
  async listarAtivos(): Promise<Emprestimo[]> {
    const todos: Emprestimo[] = [];
    for (let pagina = 0; ; pagina++) {
      const lote = await api<Emprestimo[]>(
        `/atendente/emprestimos?ativos=true&pagina=${pagina}&tamanho=${TAMANHO}`,
      );
      todos.push(...lote);
      if (lote.length < TAMANHO) return todos;
    }
  },
};
