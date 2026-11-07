import { api } from './api';

export interface EmprestimoLeitor {
  id: number;
  livroTitulo: string;
  dataRetirada: string;
  dataPrevistaDevolucao: string;
  dataDevolucao: string | null;
  situacao: string; // ATIVO | DEVOLVIDO
}

export interface ReservaLeitor {
  id: number;
  livroTitulo: string;
  situacao: string; // ativa | disponivel | atendida | cancelada | expirada
  posicaoFila: number | null; // só para reservas ativas
  dataLimiteRetirada: string | null;
}

export interface MultaLeitor {
  id: number;
  valorTotal: number;
  situacao: string; // pendente | quitada | cancelada
  geradaEm: string;
  pagaEm: string | null;
}

export interface SituacaoLeitor {
  emprestimos: EmprestimoLeitor[];
  reservas: ReservaLeitor[];
  multas: MultaLeitor[];
}

/** H12 - situação do leitor logado (o back identifica o leitor pelo token). */
export const situacaoService = {
  consultar: () => api<SituacaoLeitor>('/leitor/meu-painel'),
};
