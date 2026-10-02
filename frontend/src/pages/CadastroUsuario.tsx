import { useState } from 'react';
import { Navbar } from '../components/Navbar';
import './CadastroUsuario.css';

export function CadastroUsuario() {
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [perfil, setPerfil] = useState('Comum');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    console.log('Dados do usuário:', { nome, email, perfil });
  };

  return (
    <div className="app-container">
      <Navbar />

      <main className="content">
        <h1 className="page-title">Cadastrar Usuário</h1>

        <div className="card-container">
          <form className="form-grid" onSubmit={handleSubmit}>
            {}
            <div className="avatar-placeholder">
              <span>Avatar</span>
            </div>

            {}
            <div className="fields-container">
              <div className="form-group">
                <label htmlFor="nome">Nome completo</label>
                <input
                  id="nome"
                  type="text"
                  placeholder="Digite o nome completo"
                  value={nome}
                  onChange={(e) => setNome(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="email">E-mail</label>
                <input
                  id="email"
                  type="email"
                  placeholder="Digite o e-mail"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label htmlFor="perfil">Perfil de Acesso</label>
                <select
                  id="perfil"
                  value={perfil}
                  onChange={(e) => setPerfil(e.target.value)}
                >
                  <option value="Comum">Usuário Comum</option>
                  <option value="Administrador">Administrador</option>
                  <option value="Bibliotecario">Bibliotecário</option>
                </select>
              </div>

              {/* Botões de Ação */}
              <div className="button-group">
                <button type="button" className="btn-cancel">
                  Cancelar
                </button>
                <button type="submit" className="btn-submit">
                  Cadastrar
                </button>
              </div>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}