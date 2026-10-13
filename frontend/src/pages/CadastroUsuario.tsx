import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Navbar } from '../components/Navbar';
import { getUsuario, textoErro } from '../services/api';
import { usuariosService, type Perfil } from '../services/usuarios';
import './CadastrarLivro.css';
import './CadastroUsuario.css';

export function CadastroUsuario() {
  const navigate = useNavigate();
  const { id } = useParams();
  const usuarioId = id ? Number(id) : null;
  const editando = usuarioId !== null;
  const logado = getUsuario();
  const ehEuMesmo = editando && usuarioId === logado?.id;

  const [formData, setFormData] = useState<{
    nome: string;
    perfil: Perfil | '';
    email: string;
    senha: string;
    ativo: boolean;
  }>({ nome: '', perfil: '', email: '', senha: '', ativo: true });
  const [erro, setErro] = useState('');
  const [salvando, setSalvando] = useState(false);

  // Edição: o back não tem GET por id, então busca a lista e encontra o usuário.
  useEffect(() => {
    if (usuarioId === null) return;
    let cancelado = false;
    (async () => {
      try {
        const lista = await usuariosService.listar();
        if (cancelado) return;
        const u = lista.find((x) => x.id === usuarioId);
        if (!u) {
          setErro('Usuário não encontrado.');
          return;
        }
        setFormData({ nome: u.nome, perfil: u.perfil, email: u.email, senha: '', ativo: u.ativo });
      } catch (err) {
        if (!cancelado) setErro(textoErro(err));
      }
    })();
    return () => {
      cancelado = true;
    };
  }, [usuarioId]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.perfil) return;
    setErro('');
    setSalvando(true);
    try {
      const nome = formData.nome.trim();
      const email = formData.email.trim();
      if (usuarioId !== null) {
        const atualizado = await usuariosService.atualizar(usuarioId, {
          nome,
          email,
          perfil: formData.perfil,
          ativo: formData.ativo,
        });
        // Editando a própria conta: mantém o nome do menu em dia.
        if (ehEuMesmo && logado) {
          localStorage.setItem(
            'usuario',
            JSON.stringify({ ...logado, nome: atualizado.nome, email: atualizado.email }),
          );
        }
      } else {
        // Leitores não entram por aqui: o back exige que sejam cadastrados pelo atendente.
        await usuariosService.criar({
          nome,
          email,
          senha: formData.senha,
          perfil: formData.perfil === 'administrador' ? 'administrador' : 'atendente',
        });
      }
      navigate('/usuarios');
    } catch (err) {
      setErro(textoErro(err));
    } finally {
      setSalvando(false);
    }
  };

  // O back não permite converter leitor em funcionário nem o contrário.
  const perfilTravado = formData.perfil === 'leitor' || ehEuMesmo;

  return (
    <div className="app-container">
      <Navbar userProfile={{ nome: logado?.nome ?? 'Usuário' }} />

      <main className="cadastrar-livro-container">
        <h1 className="page-title">{editando ? 'Editar Usuário' : 'Cadastrar Usuário'}</h1>

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
                <label htmlFor="perfil">Perfil</label>
                <div className="input-with-icon">
                  <select
                    id="perfil"
                    name="perfil"
                    value={formData.perfil}
                    onChange={handleChange}
                    disabled={perfilTravado}
                    required
                  >
                    <option value="" disabled>Selecione o perfil</option>
                    {formData.perfil === 'leitor' && <option value="leitor">Leitor</option>}
                    <option value="atendente">Atendente</option>
                    <option value="administrador">Administrador</option>
                  </select>
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

              {/* O back não altera senha pela edição, então o campo só existe no cadastro. */}
              {!editando && (
                <div className="form-group">
                  <label htmlFor="senha">Senha</label>
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
              )}

              <div className="form-actions">
                <button
                  type="button"
                  className="btn-cancelar"
                  onClick={() => navigate('/usuarios')}
                >
                  Cancelar
                </button>
                <button type="submit" className="btn-cadastrar" disabled={salvando}>
                  {salvando ? 'Salvando...' : editando ? 'Salvar' : 'Cadastrar'}
                </button>
              </div>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}
