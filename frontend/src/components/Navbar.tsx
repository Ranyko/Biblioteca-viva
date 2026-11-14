import { Link, useLocation } from 'react-router-dom';
import iconeBiblioteca from '../assets/book_open.svg';

interface NavbarProps {
  userProfile?: {
    nome: string;
    avatarUrl?: string;
  };
}

export function Navbar({ userProfile }: NavbarProps) {
  const location = useLocation();

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
            <Link 
              to="/dashboard" 
              className={`nav-item ${location.pathname === '/dashboard' ? 'active' : ''}`}
            >
              Dashboard
            </Link>

            <Link 
              to="/cadastrar-usuario" 
              className={`nav-item ${location.pathname === '/cadastrar-usuario' ? 'active' : ''}`}
            >
              Usuários
            </Link>

            <Link 
              to="/acervo" 
              className={`nav-item ${location.pathname === '/acervo' ? 'active' : ''}`}
            >
              Acervo
            </Link>

            
            <Link 
              to="/filareserva" 
              className={`nav-item ${location.pathname === '/filareserva' ? 'active' : ''}`}
            >
              Fila de Reservas
            </Link>

            <Link 
              to="/emprestimo" 
              className={`nav-item ${location.pathname === '/emprestimo' ? 'active' : ''}`}
            >
              Empréstimo
            </Link>

            <Link 
              to="/devolucao" 
              className={`nav-item ${location.pathname === '/devolucao' ? 'active' : ''}`}
            >
              Devolução
            </Link>
          </nav>

          <div className="user-profile">
            <span className="user-role">{userProfile.nome}</span>
            <div className="avatar-circle">
              {userProfile.avatarUrl ? (
                <img src={userProfile.avatarUrl} alt="Avatar" />
              ) : (
                <span>{userProfile.nome ? userProfile.nome.charAt(0).toUpperCase() : 'U'}</span>
              )}
            </div>
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