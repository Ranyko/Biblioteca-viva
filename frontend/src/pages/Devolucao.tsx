import { useState } from 'react';
import { Navbar } from '../components/Navbar';
import './Devolucao.css';

export function Devolucao() {
  const [leitor, setLeitor] = useState('Carlos Henrique');
  const [livro, setLivro] = useState('Dom Casmurro');
  

  const [diasAtraso] = useState<number>(183);
  const [multa] = useState<string>('R$ 25,00');

  const dataRetirada = '14/02/2025';
  const dataDevolucao = '25/09/2025';

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    console.log('Registrando devolução para:', { leitor, livro });
  };

  return (
    <div className="app-container">
      <Navbar userProfile={{ nome: 'Atendente' }} />

      <main className="devolucao-container">
        <h1 className="page-title">Registro de Devolução</h1>

        <div className="devolucao-card">
          <form className="devolucao-form" onSubmit={handleSubmit}>
            
            <div className="devolucao-content-grid">
              
              
              <div className="fields-column">
                
                {/* Campo Leitor */}
                <div className="form-field">
                  <label id='leitor' className="field-label" htmlFor="leitor">Leitor</label>
                  <div className="search-input-wrapper">
                    <input
                      id="leitor"
                      type="text"
                      placeholder="Digite o nome do leitor"
                      value={leitor}
                      onChange={(e) => setLeitor(e.target.value)}
                      required
                    />
                    <svg className="search-icon-svg" viewBox="0 0 24 24" fill="none" stroke="#6B7280" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <circle cx="11" cy="11" r="8"></circle>
                      <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                    </svg>
                  </div>
                </div>

                {/* Campo Livro */}
                <div className="form-field">
                  <label id='livro' className="field-label" htmlFor="livro">Livro</label>
                  <div className="search-input-wrapper">
                    <input
                      id="livro"
                      type="text"
                      placeholder="Digite o nome do livro"
                      value={livro}
                      onChange={(e) => setLivro(e.target.value)}
                      required
                    />
                    <svg className="search-icon-svg" viewBox="0 0 24 24" fill="none" stroke="#6B7280" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <circle cx="11" cy="11" r="8"></circle>
                      <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                    </svg>
                  </div>
                </div>

                {/* Data de Retirada */}
                <div className="form-field">
                  <label id='data-retirada' className="field-label">Data de Retirada</label>
                  <div className="date-display-box">
                    {dataRetirada}
                  </div>
                </div>

                {/* Data de Devolução */}
                <div className="form-field">
                  <label id='data-devolucao' className="field-label">Data de Devolução</label>
                  <div className="date-display-box">
                    {dataDevolucao}
                  </div>
                </div>

              </div>

              
              {diasAtraso > 0 && (
                <div className="atraso-info-card">
                  <p className="atraso-text"><strong>Dias de atraso:</strong> {diasAtraso}</p>
                  <p className="multa-text"><strong>Multa:</strong> {multa}</p>
                </div>
              )}

            </div>

            {/* Botões de Ação */}
            <div className="action-buttons">
              <button type="button" className="btn-cancel-outline">
                Cancelar
              </button>
              <button type="submit" className="btn-submit-blue">
                Registrar Devolução
              </button>
            </div>

          </form>
        </div>
      </main>
    </div>
  );
}