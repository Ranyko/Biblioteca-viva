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

export interface UsuarioCreate {
  nome: string;
  email: string;
  senha: string;
  perfil: 'atendente' | 'administrador'; // leitor é cadastrado pelo atendente
}

export const usuariosService = {
  listar: () => api<Usuario[]>('/admin/usuarios'),
  criar: (dados: UsuarioCreate) => api<Usuario>('/admin/usuarios', { method: 'POST', body: dados }),
  // PUT exige todos os campos (nome, email, perfil e ativo).
  atualizar: (id: number, dados: UsuarioUpdate) =>
    api<Usuario>(`/admin/usuarios/${id}`, { method: 'PUT', body: dados }),
  definirAtivo: (u: Usuario, ativo: boolean) =>
    api<Usuario>(`/admin/usuarios/${u.id}`, {
      method: 'PUT',
      body: { nome: u.nome, email: u.email, perfil: u.perfil, ativo } satisfies UsuarioUpdate,
    }),
};
