import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Navbar } from '../components/Navbar';
import { getUsuario, textoErro } from '../services/api';
import { pesquisarAcervo, type ObraAcervo } from '../services/acervo';
import './Acervo.css';

export function Acervo() {
  const [busca, setBusca] = useState('');
  const [obras, setObras] = useState<ObraAcervo[]>([]);
  const [truncado, setTruncado] = useState(false);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');
  const navigate = useNavigate();

  // Pesquisa no back a cada mudança do termo (com pequena espera enquanto a pessoa digita).
  useEffect(() => {
    let cancelado = false;
    const timer = setTimeout(async () => {
      setCarregando(true);
      setErro('');
      try {
        const r = await pesquisarAcervo(busca);
        if (cancelado) return;
        setObras(r.obras);
        setTruncado(r.truncado);
      } catch (err) {
        if (!cancelado) setErro(textoErro(err));
      } finally {
        if (!cancelado) setCarregando(false);
      }
    }, busca ? 300 : 0);
    return () => {
      cancelado = true;
      clearTimeout(timer);
    };
  }, [busca]);

  const handleVerDetalhes = (obra: ObraAcervo) => {
    navigate('/reserva', {
      state: {
        titulo: obra.titulo,
        autor: obra.autores.join(', '),
        tipo: obra.categorias.join(', '),
        isbn: obra.isbn,
        exemplaresDisponiveis: obra.exemplaresDisponiveis,
        descricao: obra.descricao ?? undefined,
      },
    });
  };

  return (
    <div className="app-container">
      <Navbar userProfile={{ nome: getUsuario()?.nome ?? 'Usuário' }} />

      <main className="acervo-container">
        <h1 className="page-title">Acervo</h1>

        <div className="acervo-card">
          {}
          <div className="acervo-search-bar">
            <div className="search-input-wrapper">
              <input
                type="text"
                placeholder="Pesquisar por título, autor ou categoria..."
                maxLength={200}
                value={busca}
                onChange={(e) => setBusca(e.target.value)}
              />
              <svg className="search-icon-svg" viewBox="0 0 24 24" fill="none" stroke="#6B7280" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="11" cy="11" r="8"></circle>
                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
              </svg>
            </div>
          </div>

         
          {erro && <p style={{ color: '#b91c1c', margin: '0 0 1rem' }}>{erro}</p>}

          <div className="acervo-grid">
            {carregando && <p className="no-results-text">Carregando...</p>}
            {!carregando && !erro && obras.length === 0 && (
              <p className="no-results-text">
                {busca ? `Nenhum livro encontrado para "${busca}".` : 'O acervo está vazio.'}
              </p>
            )}
            {!carregando &&
              obras.map((obra) => (
                <div key={obra.id} className="livro-item-card">
                  <div className="livro-cover-placeholder">
                    <span className="placeholder-text">Capa</span>
                  </div>

                  <div className="livro-info">
                    <h3 className="livro-titulo">{obra.titulo}</h3>
                    <p className="livro-autor">{obra.autores.join(', ')}</p>
                    <p className="livro-tipo">{obra.categorias.join(', ')}</p>
                  </div>

                  <div className="livro-action-group">
                    <span className="livro-qtd">
                      Quantidade disponível: <strong>{obra.exemplaresDisponiveis}</strong>
                    </span>

                    <button
                      type="button"
                      className="btn-ver-detalhes"
                      onClick={() => handleVerDetalhes(obra)}
                    >
                      Ver detalhes
                    </button>
                  </div>
                </div>
              ))}
          </div>

          {truncado && !carregando && (
            <p style={{ color: '#6b7280', marginTop: '1rem' }}>
              Mostrando as primeiras 100 obras. Refine a busca para ver as demais.
            </p>
          )}
        </div>
      </main>
    </div>
  );
}