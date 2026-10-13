import { Link, useLocation, useNavigate } from 'react-router-dom';
import iconeBiblioteca from '../assets/book_open.svg';
import { getUsuario, limparSessao } from '../services/api';
import { MENU_POR_PERFIL } from '../auth/perfis';
import type { Perfil } from '../services/usuarios';

interface NavbarProps {
  userProfile?: {
    nome: string;
    avatarUrl?: string;
  };
}

export function Navbar({ userProfile }: NavbarProps) {
  const location = useLocation();
  const navigate = useNavigate();
  const usuario = getUsuario();
  const menu = MENU_POR_PERFIL[usuario?.perfil as Perfil] ?? [];

  const sair = () => {
    limparSessao();
    navigate('/');
  };

  return (
    <header className="navbar">
      <div className="logo-container">
        <img 
          src={iconeBiblioteca} 
          alt="Ícone Biblioteca" 
          className="logo-icon" 
          width={48} 
          height={48} 
        />
        <span className="logo-text">Biblioteca Viva</span>
      </div>

      {userProfile ? (
        <div className="nav-right">
          <nav className="nav-links">
            {menu.map((item) => {
              const ativo = (item.ativoEm ?? [item.to]).some((c) => location.pathname.startsWith(c));
              return (
                <Link
                  key={item.to}
                  to={item.to}
                  className={`nav-item ${ativo ? 'active' : ''}`}
                >
                  {item.rotulo}
                </Link>
              );
            })}
          </nav>

          <div className="user-profile">
            <span className="user-role">{usuario?.nome ?? userProfile.nome}</span>
            <div className="avatar-circle">
              {userProfile.avatarUrl ? (
                <img src={userProfile.avatarUrl} alt="Avatar" />
              ) : (
                <span>{(usuario?.nome ?? userProfile.nome)?.charAt(0).toUpperCase() || 'U'}</span>
              )}
            </div>
            <button type="button" className="btn-secondary" onClick={sair}>
              Sair
            </button>
          </div>
        </div>
      ) : (
        <Link to="/">
          <button className="btn-secondary">Sign in</button>
        </Link>
      )}
    </header>
  );
}