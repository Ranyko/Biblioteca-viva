import { api } from './api';
import type { Pagina, Referencia } from './catalogo';

export interface ClubeLeitura {
  id: number;
  nome: string;
  descricao: string | null;
  status: 'ativo' | 'encerrado' | string;
  capacidadeMaxima: number;
  periodicidade: string | null;
  responsavelId: number;
  criadoEm: string;
  categorias: Referencia[];
}

export interface EncontroClube {
  id: number;
  clubeId: number;
  clubeNome: string;
  livroId: number;
  livroTitulo: string;
  dataHora: string;
  localOuAcesso: string;
  status: 'aberto' | 'cancelado' | 'concluido' | string;
  capacidadeMaxima: number;
  inscricoesConfirmadas: number;
  vagasDisponiveis: number;
  criadoEm: string;
}

export interface MinhaInscricao {
  id: number;
  clubeId: number;
  clubeNome: string;
  encontroId: number;
  livroId: number;
  livroTitulo: string;
  dataHora: string;
  localOuAcesso: string;
  statusEncontro: string;
  statusInscricao: string;
  inscritoEm: string;
  canceladoEm: string | null;
  presente: boolean | null;
  presencaRegistradaEm: string | null;
}

export interface InscricaoEncontro {
  id: number;
  encontroId: number;
  leitorId: number;
  status: string;
  inscritoEm: string;
  canceladoEm: string | null;
  presente: boolean | null;
  presencaRegistradaEm: string | null;
}

export interface ParticipanteEncontro {
  leitorId: number;
  nome: string;
  statusInscricao: string;
  inscritoEm: string;
  presente: boolean | null;
  presencaRegistradaEm: string | null;
}

export interface ClubeLeituraRequest {
  nome: string;
  descricao: string;
  capacidadeMaxima: number;
  periodicidade: string;
  categoriaIds: number[];
}

export interface EncontroClubeRequest {
  livroId: number;
  dataHora: string;
  localOuAcesso: string;
}

const TAMANHO_PAGINA = 100;

async function listarTodos<T>(buscar: (pagina: number) => Promise<Pagina<T>>): Promise<T[]> {
  const itens: T[] = [];
  for (let pagina = 0; ; pagina++) {
    const resposta = await buscar(pagina);
    itens.push(...resposta.itens);
    if (itens.length >= resposta.total || resposta.itens.length === 0) return itens;
  }
}

function queryPagina(pagina: number) {
  return `pagina=${pagina}&tamanho=${TAMANHO_PAGINA}`;
}

export const clubesLeituraService = {
  listarClubesLeitor: () =>
    listarTodos((pagina) => api<Pagina<ClubeLeitura>>(`/leitor/clubes-leitura?${queryPagina(pagina)}`)),

  listarClubesEquipe: (administrador: boolean) => {
    const base = administrador ? '/admin/clubes-leitura' : '/atendente/clubes-leitura';
    return listarTodos((pagina) =>
      api<Pagina<ClubeLeitura>>(`${base}?${queryPagina(pagina)}`),
    );
  },

  criarClube: (dados: ClubeLeituraRequest) =>
    api<ClubeLeitura>('/admin/clubes-leitura', { method: 'POST', body: dados }),

  atualizarClube: (id: number, dados: ClubeLeituraRequest) =>
    api<ClubeLeitura>(`/admin/clubes-leitura/${id}`, { method: 'PUT', body: dados }),

  alterarStatusClube: (id: number, status: 'ativo' | 'encerrado') =>
    api<ClubeLeitura>(`/admin/clubes-leitura/${id}/status`, { method: 'PATCH', body: { status } }),

  listarEncontrosEquipe: (clubeId: number) =>
    listarTodos((pagina) =>
      api<Pagina<EncontroClube>>(`/atendente/clubes-leitura/${clubeId}/encontros?${queryPagina(pagina)}`),
    ),

  listarEncontrosLeitor: (clubeId: number) =>
    listarTodos((pagina) =>
      api<Pagina<EncontroClube>>(`/leitor/clubes-leitura/${clubeId}/encontros?${queryPagina(pagina)}`),
    ),

  buscarEncontroLeitor: (clubeId: number, encontroId: number) =>
    api<EncontroClube>(`/leitor/clubes-leitura/${clubeId}/encontros/${encontroId}`),

  criarEncontro: (clubeId: number, dados: EncontroClubeRequest) =>
    api<EncontroClube>(`/atendente/clubes-leitura/${clubeId}/encontros`, { method: 'POST', body: dados }),

  atualizarEncontro: (clubeId: number, encontroId: number, dados: EncontroClubeRequest) =>
    api<EncontroClube>(`/atendente/clubes-leitura/${clubeId}/encontros/${encontroId}`, {
      method: 'PUT',
      body: dados,
    }),

  cancelarEncontro: (clubeId: number, encontroId: number) =>
    api<EncontroClube>(`/atendente/clubes-leitura/${clubeId}/encontros/${encontroId}/cancelar`, {
      method: 'PATCH',
    }),

  concluirEncontro: (clubeId: number, encontroId: number) =>
    api<EncontroClube>(`/atendente/clubes-leitura/${clubeId}/encontros/${encontroId}/concluir`, {
      method: 'PATCH',
    }),

  listarParticipantes: (clubeId: number, encontroId: number) =>
    listarTodos((pagina) =>
      api<Pagina<ParticipanteEncontro>>(
        `/atendente/clubes-leitura/${clubeId}/encontros/${encontroId}/inscricoes?${queryPagina(pagina)}`,
      ),
    ),

  registrarPresenca: (clubeId: number, encontroId: number, leitorId: number, presente: boolean) =>
    api<ParticipanteEncontro>(
      `/atendente/clubes-leitura/${clubeId}/encontros/${encontroId}/inscricoes/${leitorId}/presenca`,
      { method: 'PATCH', body: { presente } },
    ),

  minhasInscricoes: (historico: boolean) =>
    listarTodos((pagina) =>
      api<Pagina<MinhaInscricao>>(
        `/leitor/minhas-inscricoes?historico=${historico}&${queryPagina(pagina)}`,
      ),
    ),

  inscrever: (encontroId: number) =>
    api<InscricaoEncontro>(`/leitor/encontros-clube/${encontroId}/inscricao`, { method: 'POST' }),

  cancelarInscricao: (encontroId: number) =>
    api<InscricaoEncontro>(`/leitor/encontros-clube/${encontroId}/inscricao`, { method: 'DELETE' }),
};
