import { api } from './api';

export interface Leitor {
  id: number; // id do leitor (é o que o empréstimo usa), diferente de usuarioId
  usuarioId: number;
  nome: string;
  email: string;
  documento: string;
  telefone: string | null;
  ativo: boolean;
  cadastradoEm: string;
}

export interface LeitorCreate {
  nome: string;
  email: string;
  senha: string;
  documento: string;
  telefone: string;
}

export const leitoresService = {
  listar: () => api<Leitor[]>('/atendente/leitores'),
  criar: (dados: LeitorCreate) => api<Leitor>('/atendente/leitores', { method: 'POST', body: dados }),
};
