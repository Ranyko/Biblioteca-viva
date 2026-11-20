import { api, ApiError } from './api';

export interface Avaliacao {
  id: number;
  livroId: number;
  nota: number;
  resenha: string | null;
  atualizadaEm: string;
}

export interface AvaliacoesLivro {
  livroId: number;
  media: number | null;
  total: number;
  pagina: number;
  tamanho: number;
  itens: Avaliacao[];
}

export interface AvaliacaoRequest {
  nota: number;
  resenha: string | null;
}

export const RESENHA_MAX = 1000;

export const avaliacoesService = {
  /** Consulta pública (qualquer usuário autenticado): média, total e avaliações mais recentes. */
  listar: (livroId: number, pagina = 0, tamanho = 5) =>
    api<AvaliacoesLivro>(`/acervo/livros/${livroId}/avaliacoes?pagina=${pagina}&tamanho=${tamanho}`),

  /** Avaliação do leitor logado; null quando ele ainda não avaliou (404). */
  buscarPropria: async (livroId: number): Promise<Avaliacao | null> => {
    try {
      return await api<Avaliacao>(`/leitor/livros/${livroId}/avaliacao`);
    } catch (err) {
      if (err instanceof ApiError && err.status === 404) return null;
      throw err;
    }
  },

  criar: (livroId: number, body: AvaliacaoRequest) =>
    api<Avaliacao>(`/leitor/livros/${livroId}/avaliacao`, { method: 'POST', body }),

  atualizar: (livroId: number, body: AvaliacaoRequest) =>
    api<Avaliacao>(`/leitor/livros/${livroId}/avaliacao`, { method: 'PUT', body }),

  excluir: (livroId: number) => api<void>(`/leitor/livros/${livroId}/avaliacao`, { method: 'DELETE' }),
};
