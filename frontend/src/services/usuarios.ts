import { api } from './api';

export type Perfil = 'leitor' | 'atendente' | 'administrador';

export interface Usuario {
  id: number;
  nome: string;
  email: string;
  perfil: Perfil;
  ativo: boolean;
  criadoEm: string;
}

export interface UsuarioUpdate {
  nome: string;
  email: string;
  perfil: Perfil;
  ativo: boolean;
}

export const usuariosService = {
  listar: () => api<Usuario[]>('/admin/usuarios'),
  atualizar: (id: number, dados: UsuarioUpdate) =>
    api<Usuario>(`/admin/usuarios/${id}`, { method: 'PUT', body: dados }),
  definirAtivo: (u: Usuario, ativo: boolean) =>
    api<Usuario>(`/admin/usuarios/${u.id}`, {
      method: 'PUT',
      body: { nome: u.nome, email: u.email, perfil: u.perfil, ativo } satisfies UsuarioUpdate,
    }),
};
