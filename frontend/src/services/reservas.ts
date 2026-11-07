import { api } from './api';

export interface ReservaCriada {
  id: number;
  leitorId: number;
  leitorNome: string;
  livroId: number;
  livroTitulo: string;
  status: string;
  dataSolicitacao: string;
  posicaoFila: number;
}

export interface ItemFila {
  posicao: number;
  reservaId: number;
  leitorId: number;
  leitorNome: string;
}

export interface Cancelamento {
  reservaId: number;
  status: string;
}

/**
 * ATENÇÃO: o back que recebemos só tem reservas em /atendente/**. Para o leitor reservar
 * sozinho assumimos POST /leitor/reservas {livroId} (leitor identificado pelo token).
 * Se o back usar outro caminho/corpo, ajuste só esta função.
 */
export const reservasService = {
  /** Atendente reserva em nome de um leitor (H9, contrato existente). */
  criar: (leitorId: number, livroId: number) =>
    api<ReservaCriada>('/atendente/reservas', { method: 'POST', body: { leitorId, livroId } }),

  /** Leitor reserva para si mesmo (endpoint ainda a confirmar com o back). */
  criarComoLeitor: (livroId: number) =>
    api<ReservaCriada>('/leitor/reservas', { method: 'POST', body: { livroId } }),

  /** Fila de reservas ativas de uma obra, já ordenada (H10). */
  fila: (livroId: number) => api<ItemFila[]>(`/atendente/reservas/fila/${livroId}`),

  cancelar: (reservaId: number) =>
    api<Cancelamento>(`/atendente/reservas/${reservaId}/cancelar`, { method: 'POST' }),
};
