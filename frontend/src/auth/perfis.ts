import type { Perfil } from '../services/usuarios';

/** Tela inicial de cada perfil (para onde o login e os redirecionamentos levam). */
export const HOME_POR_PERFIL: Record<Perfil, string> = {
  administrador: '/dashboard',
  atendente: '/emprestimo',
  leitor: '/acervo',
};

export function homePorPerfil(perfil: string | undefined): string {
  return perfil && perfil in HOME_POR_PERFIL ? HOME_POR_PERFIL[perfil as Perfil] : '/';
}

export interface ItemMenu {
  rotulo: string;
  to: string;
  /** Caminhos que também deixam o item destacado (ex.: telas de cadastro). */
  ativoEm?: string[];
}

/** Menu de cada perfil, conforme o Roteiro do Protótipo Navegável. */
export const MENU_POR_PERFIL: Record<Perfil, ItemMenu[]> = {
  administrador: [
    { rotulo: 'Dashboard', to: '/dashboard' },
    { rotulo: 'Usuários', to: '/usuarios', ativoEm: ['/usuarios', '/cadastrar-usuario'] },
    { rotulo: 'Livros', to: '/livros', ativoEm: ['/livros'] },
    { rotulo: 'Exemplares', to: '/exemplares' },
  ],
  atendente: [
    { rotulo: 'Empréstimo', to: '/emprestimo' },
    { rotulo: 'Devolução', to: '/devolucao' },
    { rotulo: 'Fila de Reservas', to: '/filareserva' },
  ],
  leitor: [
    { rotulo: 'Acervo', to: '/acervo', ativoEm: ['/acervo', '/reserva'] },
  ],
};
