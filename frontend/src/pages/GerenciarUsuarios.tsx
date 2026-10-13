import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Navbar } from '../components/Navbar';
import adicionarIcon from '../assets/adicionar.svg';
import { getUsuario, textoErro } from '../services/api';
import { usuariosService, type Perfil, type Usuario } from '../services/usuarios';
import './GerenciarUsuarios.css';

const ROTULO_PERFIL: Record<Perfil, string> = {
  leitor: 'Leitor',
  atendente: 'Atendente',
  administrador: 'Administrador',
};

interface Formulario {
  id: number;
  nome: string;
  email: string;
  perfil: Perfil;
  ativo: boolean;
}

export function GerenciarUsuarios() {
  const navigate = useNavigate();
  const logado = getUsuario();
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');
  const [form, setForm] = useState<Formulario | null>(null);
  const [salvando, setSalvando] = useState(false);

  useEffect(() => {
    let cancelado = false;
    (async () => {
      try {
        const lista = await usuariosService.listar();
        if (!cancelado) setUsuarios(lista);
      } catch (err) {
        if (!cancelado) setErro(textoErro(err));
      } finally {
        if (!cancelado) setCarregando(false);
      }
    })();
    return () => {
      cancelado = true;
    };
  }, []);

  const substituir = (atualizado: Usuario) =>
    setUsuarios((l) => l.map((u) => (u.id === atualizado.id ? atualizado : u)));

  const handleEdit = (u: Usuario) => {
    setErro('');
    setForm({ id: u.id, nome: u.nome, email: u.email, perfil: u.perfil, ativo: u.ativo });
  };

  const handleSalvar = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form) return;
    setErro('');
    setSalvando(true);
    try {
      const atualizado = await usuariosService.atualizar(form.id, {
        nome: form.nome.trim(),
        email: form.email.trim(),
        perfil: form.perfil,
        ativo: form.ativo,
      });
      substituir(atualizado);
      setForm(null);
    } catch (err) {
      setErro(textoErro(err));
    } finally {
      setSalvando(false);
    }
  };

  // O back não tem DELETE: "remover" inativa o usuário (ele deixa de conseguir entrar).
  const handleToggleAtivo = async (u: Usuario) => {
    const acao = u.ativo ? 'inativar' : 'reativar';
    if (!confirm(`Deseja realmente ${acao} o usuário "${u.nome}"?`)) return;
    try {
      substituir(await usuariosService.definirAtivo(u, !u.ativo));
    } catch (err) {
      setErro(textoErro(err));
    }
  };

  const ehLeitor = form?.perfil === 'leitor';

  return (
    <div className="app-container">
      <Navbar userProfile={{ nome: logado?.nome ?? 'Usuário' }} />

      <main className="gerenciar-usuarios-container">
        <div className="header-flex">
          <h1 className="page-title">Gerenciar Usuários</h1>
          <button
            type="button"
            className="btn-add-livro"
            onClick={() => navigate('/cadastrar-usuario')}
            title="Cadastrar Novo Usuário"
          >
            <img src={adicionarIcon} alt="Adicionar" />
          </button>
        </div>

        {erro && <p style={{ color: '#b91c1c', marginBottom: '1rem' }}>{erro}</p>}

        {form && (
          <form
            onSubmit={handleSalvar}
            className="gerenciar-card"
            style={{ marginBottom: '1.5rem', display: 'flex', gap: '1rem', flexWrap: 'wrap', alignItems: 'flex-end' }}
          >
            <label>
              Nome<br />
              <input
                type="text"
                value={form.nome}
                maxLength={120}
                required
                onChange={(e) => setForm({ ...form, nome: e.target.value })}
              />
            </label>
            <label>
              E-mail<br />
              <input
                type="email"
                value={form.email}
                maxLength={160}
                required
                onChange={(e) => setForm({ ...form, email: e.target.value })}
              />
            </label>
            <label>
              Perfil<br />
              {/* O back não permite converter leitor em funcionário nem o contrário. */}
              <select
                value={form.perfil}
                disabled={ehLeitor}
                onChange={(e) => setForm({ ...form, perfil: e.target.value as Perfil })}
              >
                {ehLeitor ? (
                  <option value="leitor">Leitor</option>
                ) : (
                  <>
                    <option value="atendente">Atendente</option>
                    <option value="administrador">Administrador</option>
                  </>
                )}
              </select>
            </label>
            <button type="submit" disabled={salvando}>{salvando ? 'Salvando...' : 'Salvar'}</button>
            <button type="button" onClick={() => setForm(null)}>Cancelar</button>
          </form>
        )}

        <div className="gerenciar-card">
          <div className="table-responsive">
            <table className="gerenciar-table">
              <thead>
                <tr>
                  <th className="col-titulo">Nome</th>
                  <th>E-mail</th>
                  <th>Perfil</th>
                  <th>Situação</th>
                  <th className="col-acoes">Ações</th>
                </tr>
              </thead>
              <tbody>
                {carregando && (
                  <tr><td colSpan={5}>Carregando...</td></tr>
                )}
                {!carregando && usuarios.length === 0 && !erro && (
                  <tr><td colSpan={5}>Nenhum usuário cadastrado.</td></tr>
                )}
                {usuarios.map((u) => {
                  const ehEuMesmo = u.id === logado?.id;
                  return (
                    <tr key={u.id} style={u.ativo ? undefined : { opacity: 0.5 }}>
                      <td className="col-titulo">{u.nome}</td>
                      <td>{u.email}</td>
                      <td>{ROTULO_PERFIL[u.perfil]}</td>
                      <td>{u.ativo ? 'Ativo' : 'Inativo'}</td>
                      <td className="col-acoes">
                        <button
                          type="button"
                          className="btn-icon"
                          title="Editar usuário"
                          onClick={() => handleEdit(u)}
                        >
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <path d="M12 20h9" />
                            <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" />
                          </svg>
                        </button>

                        <button
                          type="button"
                          className="btn-icon btn-delete"
                          title={
                            ehEuMesmo
                              ? 'Você não pode inativar a própria conta'
                              : u.ativo ? 'Inativar usuário' : 'Reativar usuário'
                          }
                          disabled={ehEuMesmo}
                          onClick={() => handleToggleAtivo(u)}
                          style={ehEuMesmo ? { opacity: 0.3, cursor: 'not-allowed' } : undefined}
                        >
                          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                            <polyline points="3 6 5 6 21 6" />
                            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                          </svg>
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </div>
  );
}
