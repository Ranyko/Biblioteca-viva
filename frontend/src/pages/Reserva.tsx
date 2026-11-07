import { useState } from 'react';
import { Navigate, useNavigate, useLocation } from 'react-router-dom';
import { Navbar } from '../components/Navbar';
import { getUsuario, textoErro } from '../services/api';
import { reservasService } from '../services/reservas';
import './Reserva.css';

interface LivroState {
  id: number;
  titulo: string;
  autor: string;
  tipo: string;
  isbn?: string;
  exemplaresDisponiveis: number;
  descricao?: string;
  capaUrl?: string;
}

export function Reserva() {
  const navigate = useNavigate();
  const location = useLocation();
  const obra = location.state as LivroState | null;

  const [erro, setErro] = useState('');
  const [posicao, setPosicao] = useState<number | null>(null);
  const [salvando, setSalvando] = useState(false);

  // Sem uma obra escolhida no acervo não há o que reservar.
  if (!obra?.id) return <Navigate to="/acervo" replace />;

  const disponivel = obra.exemplaresDisponiveis > 0;

  const handleReservar = async () => {
    setErro('');
    setSalvando(true);
    try {
      const reserva = await reservasService.criarComoLeitor(obra.id);
      setPosicao(reserva.posicaoFila);
    } catch (err) {
      setErro(textoErro(err));
    } finally {
      setSalvando(false);
    }
  };

  const voltar = () => navigate('/acervo');

  return (
    <div className="app-container">
      <Navbar userProfile={{ nome: getUsuario()?.nome ?? 'Leitor' }} />

      <main className="reserva-container">
        <h1 className="page-title">Detalhes da Obra</h1>

        <div className="reserva-card">
          {erro && <p style={{ color: '#b91c1c', margin: '0 0 1rem' }}>{erro}</p>}
          {posicao !== null && (
            <p style={{ color: '#047857', margin: '0 0 1rem' }}>
              Reserva efetuada! Você está na posição {posicao} da fila.
            </p>
          )}
          {disponivel && (
            <p style={{ margin: '0 0 1rem' }}>
              Há exemplar disponível: retire a obra no balcão. A reserva só vale para obras sem exemplar disponível.
            </p>
          )}

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
                <button type="button" className="btn-cancel-outline" onClick={voltar}>
                  {posicao !== null ? 'Voltar' : 'Cancelar'}
                </button>
                <button
                  type="button"
                  className="btn-submit-blue"
                  onClick={handleReservar}
                  disabled={salvando || disponivel || posicao !== null}
                >
                  {salvando ? 'Reservando...' : 'Reservar Livro'}
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
                <span className="exemplares-count">{obra.exemplaresDisponiveis}</span>
              </div>
            </div>

            <div className="description-column">
              <div className="description-box">
                <h2 className="description-title">Descrição</h2>
                <p className="description-text">{obra.descricao || 'Sem descrição cadastrada.'}</p>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
