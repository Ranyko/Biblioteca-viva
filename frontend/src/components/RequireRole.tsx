import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { homePorPerfil } from '../auth/perfis';
import { getToken, getUsuario } from '../services/api';
import type { Perfil } from '../services/usuarios';

interface Props {
  perfis: Perfil[];
  children: ReactNode;
}

/* Só renderiza a tela para os perfis permitidos; senão manda para o login ou para a tela inicial do perfil. */
export function RequireRole({ perfis, children }: Props) {
  const usuario = getUsuario();
  if (!getToken() || !usuario) return <Navigate to="/" replace />;
  if (!perfis.includes(usuario.perfil as Perfil)) {
    return <Navigate to={homePorPerfil(usuario.perfil)} replace />;
  }
  return <>{children}</>;
}
