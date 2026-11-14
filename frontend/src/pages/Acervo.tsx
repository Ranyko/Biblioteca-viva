import { useState } from 'react';
import { useNavigate } from 'react-router-dom'; 
import { Navbar } from '../components/Navbar';
import './Acervo.css';

interface Livro {
  id: number;
  titulo: string;
  autor: string;
  tipo: string;
  quantidadeDisponivel: number;
  capaUrl?: string;
}

const ACERVO_MOCK: Livro[] = [
  {
    id: 1,
    titulo: 'Dom Casmurro',
    autor: 'Machado de Assis',
    tipo: 'Livro',
    quantidadeDisponivel: 2,
  },
  {
    id: 2,
    titulo: 'Memórias Póstumas de Brás Cubas',
    autor: 'Machado de Assis',
    tipo: 'Livro',
    quantidadeDisponivel: 4,
  },
  {
    id: 3,
    titulo: 'O Cortiço',
    autor: 'Aluísio Azevedo',
    tipo: 'Livro',
    quantidadeDisponivel: 1,
  },
  {
    id: 4,
    titulo: 'Grande Sertão: Veredas',
    autor: 'João Guimarães Rosa',
    tipo: 'Livro',
    quantidadeDisponivel: 3,
  },
  {
    id: 5,
    titulo: 'A Hora da Estrela',
    autor: 'Clarice Lispector',
    tipo: 'Livro',
    quantidadeDisponivel: 5,
  },
  {
    id: 6,
    titulo: 'Vidas Secas',
    autor: 'Graciliano Ramos',
    tipo: 'Livro',
    quantidadeDisponivel: 0,
  },
];

export function Acervo() {
  const [busca, setBusca] = useState('');
  const navigate = useNavigate(); 

 
  const handleVerDetalhes = (livro: Livro) => {
    navigate('/reserva', { state: livro });
  };

 
  const livrosFiltrados = ACERVO_MOCK.filter((livro) =>
    livro.titulo.toLowerCase().includes(busca.toLowerCase())
  );

  return (
    <div className="app-container">
      <Navbar userProfile={{ nome: 'Leitor' }} />

      <main className="acervo-container">
        <h1 className="page-title">Acervo</h1>

        <div className="acervo-card">
          {}
          <div className="acervo-search-bar">
            <div className="search-input-wrapper">
              <input
                type="text"
                placeholder="Pesquisar livros pelo título..."
                value={busca}
                onChange={(e) => setBusca(e.target.value)}
              />
              <svg className="search-icon-svg" viewBox="0 0 24 24" fill="none" stroke="#6B7280" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="11" cy="11" r="8"></circle>
                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
              </svg>
            </div>
          </div>

         
          <div className="acervo-grid">
            {livrosFiltrados.length > 0 ? (
              livrosFiltrados.map((livro) => (
                <div key={livro.id} className="livro-item-card">
                  
                  {/* Capa */}
                  <div className="livro-cover-placeholder">
                    {livro.capaUrl ? (
                      <img src={livro.capaUrl} alt={livro.titulo} />
                    ) : (
                      <span className="placeholder-text">Capa</span>
                    )}
                  </div>

                  
                  <div className="livro-info">
                    <h3 className="livro-titulo">{livro.titulo}</h3>
                    <p className="livro-autor">{livro.autor}</p>
                    <p className="livro-tipo">{livro.tipo}</p>
                  </div>

                 
                  <div className="livro-action-group">
                    <span className="livro-qtd">
                      Quantidade disponível: <strong>{livro.quantidadeDisponivel}</strong>
                    </span>

                    {}
                    <button 
                      type="button" 
                      className="btn-ver-detalhes"
                      onClick={() => handleVerDetalhes(livro)}
                    >
                      Ver detalhes
                    </button>
                  </div>

                </div>
              ))
            ) : (
              <p className="no-results-text">Nenhum livro encontrado para "{busca}".</p>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}