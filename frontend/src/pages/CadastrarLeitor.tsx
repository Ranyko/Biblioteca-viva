import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Navbar } from '../components/Navbar';
import { getUsuario, textoErro } from '../services/api';
import { leitoresService } from '../services/leitores';
import './CadastrarLivro.css';

export function CadastrarLeitor() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    nome: '',
    email: '',
    documento: '',
    telefone: '',
    senha: '',
  });
  const [erro, setErro] = useState('');
  const [salvando, setSalvando] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErro('');
    setSalvando(true);
    try {
      await leitoresService.criar({
        nome: formData.nome.trim(),
        email: formData.email.trim(),
        documento: formData.documento.trim(),
        telefone: formData.telefone.trim(),
        senha: formData.senha,
      });
      navigate('/leitores');
    } catch (err) {
      setErro(textoErro(err));
    } finally {
      setSalvando(false);
    }
  };

  return (
    <div className="app-container">
      <Navbar userProfile={{ nome: getUsuario()?.nome ?? 'Usuário' }} />

      <main className="cadastrar-livro-container">
        <h1 className="page-title">Cadastrar Leitor</h1>

        <div className="cadastrar-livro-card">
          <form onSubmit={handleSubmit} className="cadastrar-livro-form">
            <div className="capa-column">
              <div className="capa-placeholder">
                <span className="placeholder-text">Avatar</span>
              </div>
            </div>

            <div className="fields-column">
              {erro && <p style={{ color: '#b91c1c', margin: 0 }}>{erro}</p>}

              <div className="form-group">
                <label htmlFor="nome">Nome</label>
                <div className="input-with-icon">
                  <input
                    type="text"
                    id="nome"
                    name="nome"
                    placeholder="Digite o nome completo"
                    value={formData.nome}
                    onChange={handleChange}
                    maxLength={120}
                    required
                  />
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="email">Email</label>
                <div className="input-with-icon">
                  <input
                    type="email"
                    id="email"
                    name="email"
                    placeholder="Digite o e-mail"
                    value={formData.email}
                    onChange={handleChange}
                    maxLength={160}
                    required
                  />
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="documento">Documento</label>
                <div className="input-with-icon">
                  <input
                    type="text"
                    id="documento"
                    name="documento"
                    placeholder="CPF ou RG (só letras e números contam)"
                    value={formData.documento}
                    onChange={handleChange}
                    maxLength={30}
                    required
                  />
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="telefone">Telefone</label>
                <div className="input-with-icon">
                  <input
                    type="tel"
                    id="telefone"
                    name="telefone"
                    placeholder="(16) 99999-9999"
                    value={formData.telefone}
                    onChange={handleChange}
                    maxLength={25}
                    required
                  />
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="senha">Senha de acesso</label>
                <div className="input-with-icon">
                  <input
                    type="password"
                    id="senha"
                    name="senha"
                    placeholder="Mínimo de 8 caracteres"
                    value={formData.senha}
                    onChange={handleChange}
                    minLength={8}
                    maxLength={72}
                    autoComplete="new-password"
                    required
                  />
                </div>
              </div>

              <div className="form-actions">
                <button
                  type="button"
                  className="btn-cancelar"
                  onClick={() => navigate('/leitores')}
                >
                  Cancelar
                </button>
                <button type="submit" className="btn-cadastrar" disabled={salvando}>
                  {salvando ? 'Salvando...' : 'Cadastrar'}
                </button>
              </div>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}
