import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Navbar } from '../components/Navbar';
import adicionarIcon from '../assets/adicionar.svg';
import { getUsuario, textoErro } from '../services/api';
import { livrosService, type Livro } from '../services/catalogo';
import './GerenciarLivros.css';

export function GerenciarLivros() {
  const navigate = useNavigate();
  const [livros, setLivros] = useState<Livro[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');

  useEffect(() => {
    let cancelado = false;
    (async () => {
      try {
        const pagina = await livrosService.listar(0, 100);
        if (!cancelado) setLivros(pagina.itens);
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

  const handleAdicionar = () => {
    navigate('/livros/cadastrar');
  };

  const handleEdit = (id: number) => {
    navigate(`/livros/${id}/editar`);
  };

  // O backend não tem DELETE: "remover" inativa a obra (some do acervo, mantém o histórico).
  const handleToggleAtivo = async (livro: Livro) => {
    const acao = livro.ativo ? 'inativar' : 'reativar';
    if (!confirm(`Deseja realmente ${acao} "${livro.titulo}"?`)) return;
    try {
      const atualizado = await livrosService.definirAtivo(livro, !livro.ativo);
      setLivros((atual) => atual.map((l) => (l.id === atualizado.id ? atualizado : l)));
    } catch (err) {
      setErro(textoErro(err));
    }
  };

  return (
    <div className="app-container">
      <Navbar userProfile={{ nome: getUsuario()?.nome ?? 'Usuário' }} />

      <main className="gerenciar-livros-container">
        <div className="header-flex">
          <h1 className="page-title">Gerenciar Livros</h1>
          <button
            type="button"
            className="btn-add-livro"
            onClick={handleAdicionar}
            title="Cadastrar Novo Livro"
          >
            <img src={adicionarIcon} alt="Adicionar" />
          </button>
        </div>

        {erro && <p style={{ color: '#b91c1c', marginBottom: '1rem' }}>{erro}</p>}

        <div className="gerenciar-card">
          <div className="table-responsive">
            <table className="gerenciar-table">
              <thead>
                <tr>
                  <th className="col-titulo">Título</th>
                  <th>ISBN</th>
                  <th>Autor</th>
                  <th>Categoria</th>
                  <th className="col-acoes">Ações</th>
                </tr>
              </thead>
              <tbody>
                {carregando && (
                  <tr><td colSpan={5}>Carregando...</td></tr>
                )}
                {!carregando && livros.length === 0 && !erro && (
                  <tr><td colSpan={5}>Nenhum livro cadastrado.</td></tr>
                )}
                {livros.map((item) => (
                  <tr key={item.id} style={item.ativo ? undefined : { opacity: 0.5 }}>
                    <td className="col-titulo">{item.titulo}{item.ativo ? '' : ' (inativo)'}</td>
                    <td>{item.isbn}</td>
                    <td>{item.autores.map((a) => a.nome).join(', ')}</td>
                    <td>{item.categorias.map((c) => c.nome).join(', ')}</td>
                    <td className="col-acoes">
                      <button
                        type="button"
                        className="btn-icon"
                        title="Editar livro"
                        onClick={() => handleEdit(item.id)}
                      >
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M12 20h9" />
                          <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" />
                        </svg>
                      </button>

                      <button
                        type="button"
                        className="btn-icon btn-delete"
                        title={item.ativo ? 'Inativar livro' : 'Reativar livro'}
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