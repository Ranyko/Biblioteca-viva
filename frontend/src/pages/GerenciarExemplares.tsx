import { useEffect, useMemo, useState } from 'react';
import { Navbar } from '../components/Navbar';
import adicionarIcon from '../assets/adicionar.svg';
import { getUsuario, textoErro } from '../services/api';
import {
  exemplaresService,
  livrosService,
  type EstadoConservacao,
  type Exemplar,
  type Livro,
  type StatusExemplar,
} from '../services/catalogo';
import './GerenciarExemplares.css';

const ESTADOS: EstadoConservacao[] = ['novo', 'bom', 'regular', 'danificado'];
const ROTULO_STATUS: Record<StatusExemplar, string> = {
  disponivel: 'Disponível',
  emprestado: 'Emprestado',
  reservado: 'Reservado',
  inativo: 'Inativo',
};
const cap = (t: string) => t.charAt(0).toUpperCase() + t.slice(1);

interface Formulario {
  id: number | null; // null = novo
  livroId: string;
  codigoTombo: string;
  estadoConservacao: EstadoConservacao;
}
const FORM_VAZIO: Formulario = { id: null, livroId: '', codigoTombo: '', estadoConservacao: 'novo' };

export function GerenciarExemplares() {
  const [exemplares, setExemplares] = useState<Exemplar[]>([]);
  const [livros, setLivros] = useState<Livro[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');
  const [form, setForm] = useState<Formulario | null>(null);
  const [salvando, setSalvando] = useState(false);

  const tituloPorLivro = useMemo(
    () => new Map(livros.map((l) => [l.id, l.titulo])),
    [livros],
  );

  useEffect(() => {
    let cancelado = false;
    (async () => {
      try {
        const [ex, lv] = await Promise.all([exemplaresService.listar(0, 100), livrosService.listar(0, 100)]);
        if (cancelado) return;
        setExemplares(ex.itens);
        setLivros(lv.itens);
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

  const handleEdit = (item: Exemplar) => {
    setErro('');
    setForm({
      id: item.id,
      livroId: String(item.livroId),
      codigoTombo: item.codigoTombo,
      estadoConservacao: item.estadoConservacao,
    });
  };

  const handleSalvar = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form) return;
    setErro('');
    setSalvando(true);
    try {
      const dados = { codigoTombo: form.codigoTombo.trim(), estadoConservacao: form.estadoConservacao };
      if (form.id === null) {
        const criado = await exemplaresService.criar({ ...dados, livroId: Number(form.livroId) });
        setExemplares((l) => [criado, ...l]);
      } else {
        const atualizado = await exemplaresService.atualizar(form.id, dados);
        setExemplares((l) => l.map((x) => (x.id === atualizado.id ? atualizado : x)));
      }
      setForm(null);
    } catch (err) {
      setErro(textoErro(err));
    } finally {
      setSalvando(false);
    }
  };

  const handleToggleAtivo = async (item: Exemplar) => {
    const inativar = item.status !== 'inativo';
    if (!confirm(`Deseja realmente ${inativar ? 'inativar' : 'disponibilizar'} o exemplar ${item.codigoTombo}?`)) return;
    try {
      const atualizado = await exemplaresService.definirAtivo(item.id, !inativar);
      setExemplares((l) => l.map((x) => (x.id === atualizado.id ? atualizado : x)));
    } catch (err) {
      setErro(textoErro(err));
    }
  };

  return (
    <div className="app-container">
      <Navbar userProfile={{ nome: getUsuario()?.nome ?? 'Usuário' }} />

      <main className="gerenciar-exemplares-container">
        <div className="header-flex">
          <h1 className="page-title">Gerenciar Exemplares</h1>
          <button
            type="button"
            className="btn-add-livro"
            onClick={() => { setErro(''); setForm({ ...FORM_VAZIO }); }}
            title="Cadastrar Novo Exemplar"
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
              Livro<br />
              <select
                value={form.livroId}
                disabled={form.id !== null}
                required
                onChange={(e) => setForm({ ...form, livroId: e.target.value })}
              >
                <option value="">Selecione...</option>
                {livros.filter((l) => l.ativo || String(l.id) === form.livroId).map((l) => (
                  <option key={l.id} value={l.id}>{l.titulo}</option>
                ))}
              </select>
            </label>
            <label>
              Código de tombo<br />
              <input
                type="text"
                value={form.codigoTombo}
                maxLength={50}
                required
                onChange={(e) => setForm({ ...form, codigoTombo: e.target.value })}
              />
            </label>
            <label>
              Estado de conservação<br />
              <select
                value={form.estadoConservacao}
                onChange={(e) => setForm({ ...form, estadoConservacao: e.target.value as EstadoConservacao })}
              >
                {ESTADOS.map((s) => <option key={s} value={s}>{cap(s)}</option>)}
              </select>
            </label>
            <button type="submit" disabled={salvando}>{salvando ? 'Salvando...' : form.id === null ? 'Cadastrar' : 'Salvar'}</button>
            <button type="button" onClick={() => setForm(null)}>Cancelar</button>
          </form>
        )}

        <div className="gerenciar-card">
          <div className="table-responsive">
            <table className="gerenciar-table">
              <thead>
                <tr>
                  <th className="col-codigo">Código</th>
                  <th className="col-titulo">Título</th>
                  <th>Estado de conservação</th>
                  <th>Status</th>
                  <th className="col-acoes">Ações</th>
                </tr>
              </thead>
              <tbody>
                {carregando && (
                  <tr><td colSpan={5}>Carregando...</td></tr>
                )}
                {!carregando && exemplares.length === 0 && !erro && (
                  <tr><td colSpan={5}>Nenhum exemplar cadastrado.</td></tr>
                )}
                {exemplares.map((item) => (
                  <tr key={item.id}>
                    <td className="col-codigo">{item.codigoTombo}</td>
                    <td className="col-titulo">{tituloPorLivro.get(item.livroId) ?? `Livro #${item.livroId}`}</td>
                    <td>{cap(item.estadoConservacao)}</td>
                    <td>{ROTULO_STATUS[item.status]}</td>
                    <td className="col-acoes">
                      <button
                        type="button"
                        className="btn-icon"
                        title="Editar exemplar"
                        onClick={() => handleEdit(item)}
                      >
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M12 20h9" />
                          <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" />
                        </svg>
                      </button>

                      <button
                        type="button"
                        className="btn-icon btn-delete"
                        title={item.status === 'inativo' ? 'Disponibilizar exemplar' : 'Inativar exemplar'}
                        onClick={() => handleToggleAtivo(item)}
                      >
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <polyline points="3 6 5 6 21 6" />
                          <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                        </svg>
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </div>
  );
}