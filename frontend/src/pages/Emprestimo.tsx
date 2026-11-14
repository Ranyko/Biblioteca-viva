import { useState } from 'react';
import { Navbar } from '../components/Navbar';
import './Emprestimo.css';

export function Emprestimo() {
  const [leitor, setLeitor] = useState('');
  const [exemplar, setExemplar] = useState('');

  const hoje = new Date();
  const devolucaoData = new Date();
  devolucaoData.setDate(hoje.getDate() + 15);

  const formatDay = (d: Date) => String(d.getDate()).padStart(2, '0');
  const formatMonth = (d: Date) => String(d.getMonth() + 1).padStart(2, '0');
  const formatYear = (d: Date) => d.getFullYear();

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    console.log('Registrando empréstimo:', { leitor, exemplar });
  };

  return (
    <div className="app-container">
      <Navbar userProfile={{ nome: 'Atendente' }} />

      <main className="emprestimo-container">
        <h1 className="page-title">Registro de Empréstimo</h1>

        <div className="emprestimo-card">
          <form className="emprestimo-form" onSubmit={handleSubmit}>
            
            {/* Campo Leitor */}
            <div className="form-field">
              <label className="field-label" htmlFor="leitor">Leitor</label>
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

            {/* Campo Exemplar */}
            <div className="form-field">
              <label className="field-label" htmlFor="exemplar">Exemplar</label>
              <div className="search-input-wrapper">
                <input
                  id="exemplar"
                  type="text"
                  placeholder="Digite o nome do exemplar"
                  value={exemplar}
                  onChange={(e) => setExemplar(e.target.value)}
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
              <div className="field-label-group">
                <label className="field-label">Data de Retirada</label>
                <span className="auto-badge">
                  <svg className="badge-icon-svg" viewBox="0 0 24 24" fill="#89B4DB">
                    <path d="M19 4h-1V2h-2v2H8V2H6v2H5c-1.11 0-1.99.9-1.99 2L3 20c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V10h14v10zm0-12H5V6h14v2z"/>
                  </svg>
                  Preenchida automaticamente
                </span>
              </div>
              <div className="date-inputs-grid">
                <input className="date-input" type="text" value={formatDay(hoje)} readOnly />
                <input className="date-input" type="text" value={formatMonth(hoje)} readOnly />
                <input className="date-input" type="text" value={formatYear(hoje)} readOnly />
              </div>
            </div>

            {/* Data de Devolução */}
            <div className="form-field">
              <div className="field-label-group">
                <label className="field-label">Data de Devolução</label>
                <span className="auto-badge">
                  <svg className="badge-icon-svg" viewBox="0 0 24 24" fill="#89B4DB">
                    <path d="M19 4h-1V2h-2v2H8V2H6v2H5c-1.11 0-1.99.9-1.99 2L3 20c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V10h14v10zm0-12H5V6h14v2z"/>
                  </svg>
                  Preenchida automaticamente
                </span>
              </div>
              <div className="date-inputs-grid">
                <input className="date-input" type="text" value={formatDay(devolucaoData)} readOnly />
                <input className="date-input" type="text" value={formatMonth(devolucaoData)} readOnly />
                <input className="date-input" type="text" value={formatYear(devolucaoData)} readOnly />
              </div>
            </div>

            {/* Botões */}
            <div className="action-buttons">
              <button type="button" className="btn-cancel-outline">
                Cancelar
              </button>
              <button type="submit" className="btn-submit-blue">
                Registrar Empréstimo
              </button>
            </div>

          </form>
        </div>
      </main>
    </div>
  );
}