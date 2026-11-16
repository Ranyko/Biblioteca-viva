import { api } from './api';

export interface ObraRanking {
  livroId: number;
  titulo: string;
  totalEmprestimos: number;
}

export interface EmprestimosCategoria {
  categoriaId: number;
  categoria: string;
  totalEmprestimos: number;
}

export interface PainelBiblioteca {
  emprestimosAtivos: number;
  emprestimosAtrasados: number;
  rankingObras: ObraRanking[];
  emprestimosPorCategoria: EmprestimosCategoria[];
}

export interface Sugestao {
  livroId: number;
  titulo: string;
  descricao: string | null;
  categoria: string;
  exemplaresDisponiveis: number;
}

export const painelService = {
  /** H14 - painel do administrador (consulta agregada no back). */
  consultar: () => api<PainelBiblioteca>('/admin/painel'),
};

export const sugestoesService = {
  /** H15 - sugestões baseadas no histórico do leitor logado. */
  listar: () => api<Sugestao[]>('/leitor/sugestoes'),
};
