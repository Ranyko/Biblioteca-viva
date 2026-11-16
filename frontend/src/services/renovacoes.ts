import { api } from './api';

export interface Renovacao {
  id: number;
  emprestimoId: number;
  numeroRenovacao: number;
  limiteRenovacoesSnapshot: number;
  dataPrevistaAnterior: string;
  dataPrevistaNova: string;
  atendenteRenovacaoId: number | null;
  renovadaEm: string;
}

export const renovacoesService = {
  renovarComoLeitor: (emprestimoId: number) =>
    api<Renovacao>(`/leitor/emprestimos/${emprestimoId}/renovacao`, { method: 'POST' }),

  historicoComoLeitor: (emprestimoId: number) =>
    api<Renovacao[]>(`/leitor/emprestimos/${emprestimoId}/renovacoes`),

  renovarComoAtendente: (emprestimoId: number) =>
    api<Renovacao>(`/atendente/emprestimos/${emprestimoId}/renovacao`, { method: 'POST' }),

  historicoComoAtendente: (emprestimoId: number) =>
    api<Renovacao[]>(`/atendente/emprestimos/${emprestimoId}/renovacoes`),
};
