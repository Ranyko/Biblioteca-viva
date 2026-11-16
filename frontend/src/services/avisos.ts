import { api } from './api';
import type { Pagina } from './catalogo';

export type TipoAviso =
  | 'DEVIDA_AMANHA'
  | 'VENCIDO'
  | 'RESERVA_DISPONIVEL'
  | 'RESERVA_EXPIRANDO'
  | 'RESERVA_EXPIRADA'
  | 'MULTA_GERADA'
  | 'FILA_POSICAO';

export interface Aviso {
  id: number;
  tipo: TipoAviso | string;
  titulo: string;
  mensagem: string;
  referenciaTipo: 'emprestimo' | 'reserva' | 'multa' | null;
  referenciaId: number | null;
  diaReferencia: string | null;
  criadoEm: string;
  lidoEm: string | null; // lido = lidoEm != null
}

export interface PendenciasAtendente {
  atrasados: {
    emprestimoId: number;
    leitorNome: string;
    livroTitulo: string;
    dataPrevistaDevolucao: string;
    diasAtraso: number;
  }[];
  reservasExpirando: {
    reservaId: number;
    leitorNome: string;
    livroTitulo: string;
    dataLimiteRetirada: string;
  }[];
}

export interface PreferenciasAvisos {
  devidaAmanha: boolean;
  vencido: boolean;
  reservaDisponivel: boolean;
  reservaExpirando: boolean;
  reservaExpirada: boolean;
  multaGerada: boolean;
  filaPosicao: boolean;
  atualizadoEm?: string | null;
}

export interface Varredura {
  devidaAmanha: number;
  vencido: number;
  reservaExpirando: number;
  reservaExpirada: number;
  filaPosicao: number;
  total: number;
}

/** Avisa o sino (Navbar) que a contagem de não lidos mudou. */
export const EVENTO_AVISOS = 'avisos:atualizado';
const notificar = () => window.dispatchEvent(new Event(EVENTO_AVISOS));

export const avisosService = {
  /** lido: true = só lidos, false = só não lidos, undefined = todos. */
  listar: (opcoes: { lido?: boolean; pagina?: number; tamanho?: number } = {}) => {
    const qs = new URLSearchParams({
      pagina: String(opcoes.pagina ?? 0),
      tamanho: String(opcoes.tamanho ?? 10),
    });
    if (opcoes.lido !== undefined) qs.set('lido', String(opcoes.lido));
    return api<Pagina<Aviso>>(`/leitor/avisos?${qs}`);
  },

  contarNaoLidos: async () => (await api<{ naoLidos: number }>('/avisos/nao-lidos/contagem')).naoLidos,

  marcarLido: async (id: number) => {
    const aviso = await api<Aviso>(`/leitor/avisos/${id}/leitura`, { method: 'PATCH' });
    notificar();
    return aviso;
  },

  marcarTodosLidos: async () => {
    const r = await api<{ atualizados: number }>('/leitor/avisos/leitura-todos', { method: 'POST' });
    notificar();
    return r.atualizados;
  },

  pendencias: () => api<PendenciasAtendente>('/atendente/avisos/pendencias'),

  preferencias: () => api<PreferenciasAvisos>('/admin/avisos/preferencias'),

  salvarPreferencias: (p: PreferenciasAvisos) =>
    api<PreferenciasAvisos>('/admin/avisos/preferencias', {
      method: 'PUT',
      body: {
        devidaAmanha: p.devidaAmanha,
        vencido: p.vencido,
        reservaDisponivel: p.reservaDisponivel,
        reservaExpirando: p.reservaExpirando,
        reservaExpirada: p.reservaExpirada,
        multaGerada: p.multaGerada,
        filaPosicao: p.filaPosicao,
      },
    }),

  executarVarredura: () => api<Varredura>('/admin/avisos/varredura', { method: 'POST' }),
};

export const ROTULO_TIPO: Record<string, string> = {
  DEVIDA_AMANHA: 'Devolução amanhã',
  VENCIDO: 'Empréstimo vencido',
  RESERVA_DISPONIVEL: 'Reserva disponível',
  RESERVA_EXPIRANDO: 'Reserva expirando',
  RESERVA_EXPIRADA: 'Reserva expirada',
  MULTA_GERADA: 'Multa gerada',
  FILA_POSICAO: 'Fila de reserva',
};

/** Cor do selo por gravidade: alerta (vermelho), atenção (âmbar), informativo (azul). */
export const COR_TIPO: Record<string, 'alerta' | 'atencao' | 'info'> = {
  VENCIDO: 'alerta',
  MULTA_GERADA: 'alerta',
  RESERVA_EXPIRADA: 'alerta',
  DEVIDA_AMANHA: 'atencao',
  RESERVA_EXPIRANDO: 'atencao',
  RESERVA_DISPONIVEL: 'info',
  FILA_POSICAO: 'info',
};
