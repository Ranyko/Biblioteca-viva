import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Navbar } from '../components/Navbar';
import './Reserva.css';

interface LivroState {
  titulo: string;
  autor: string;
  tipo: string;
  isbn?: string;
  exemplaresDisponiveis?: number;
  exemplaresTotais?: number;
  descricao?: string;
  capaUrl?: string;
}

export function Reserva() {
  const navigate = useNavigate();
  const location = useLocation();

  // Caso não tenha vindo nenhum, utiliza um valor default (fallback)
  const livroNav = location.state as LivroState | undefined;

  const [obra] = useState<LivroState>({
    titulo: livroNav?.titulo || 'Dom Casmurro',
    autor: livroNav?.autor || 'Machado de Assis',
    tipo: livroNav?.tipo || 'Livro',
    isbn: livroNav?.isbn || '978-8535902778',
    exemplaresDisponiveis: livroNav?.exemplaresDisponiveis ?? 2,
    exemplaresTotais: livroNav?.exemplaresTotais ?? 5,
    descricao:
      livroNav?.descricao ||
      'Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry\'s standard dummy text ever since the 1500s.',
    capaUrl: livroNav?.capaUrl || ''
  });

  const handleReservar = () => {
    alert(`Reserva efetuada com sucesso para: ${obra.titulo}`);
    // Redireciona para a tela de Fila de Reservas após reservar
    navigate('/filareserva');
  };

  const handleCancelar = () => {
    // Retorna para a tela de acervo
    navigate('/acervo');
  };

  return (
    <div className="app-container">
      <Navbar userProfile={{ nome: 'Leitor' }} />

      <main className="reserva-container">
        <h1 className="page-title">Detalhes da Obra</h1>

        <div className="reserva-card">
          <div className="reserva-grid">
            
           
            <div className="cover-and-actions-column">
              <div className="book-cover-placeholder">
                {obra.capaUrl ? (
                  <img src={obra.capaUrl} alt={obra.titulo} />
                ) : (
                  <span className="placeholder-text">Capa do Livro</span>
                )}
              </div>

            
              <div className="action-buttons">
                <button 
                  type="button" 
                  className="btn-cancel-outline"
                  onClick={handleCancelar}
                >
                  Cancelar
                </button>
                <button 
                  type="button" 
                  className="btn-submit-blue"
                  onClick={handleReservar}
                >
                  Reservar Livro
                </button>
              </div>
            </div>

            
            <div className="details-column">
              <div className="info-group">
                <p className="info-label">Título</p>
                <p className="info-value">{obra.titulo}</p>
              </div>

              <div className="info-group">
                <p className="info-label">Autor</p>
                <p className="info-value">{obra.autor}</p>
              </div>

              <div className="info-group">
                <p className="info-label">Tipo livro</p>
                <p className="info-value">{obra.tipo}</p>
              </div>

              <div className="info-group">
                <p className="info-label">ISBN</p>
                <p className="info-value">{obra.isbn}</p>
              </div>

              
              <div className="exemplares-box">
                <span className="exemplares-title">Exemplares Disponíveis</span>
                <span className="exemplares-count">
                  {obra.exemplaresDisponiveis} de {obra.exemplaresTotais}
                </span>
              </div>
            </div>

          
            <div className="description-column">
              <div className="description-box">
                <h2 className="description-title">Descrição</h2>
                <p className="description-text">{obra.descricao}</p>
              </div>
            </div>

          </div>
        </div>
      </main>
    </div>
  );
}