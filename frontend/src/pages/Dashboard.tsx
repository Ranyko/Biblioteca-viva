import { Navbar } from '../components/Navbar';
import './Dashboard.css';

import icon1 from '../assets/icone-dashboard2.svg';
import icon2 from '../assets/icone-dashboard4.svg';
import icon3 from '../assets/icone-dashboard3.svg';
import icon4 from '../assets/icone-dashboard1.svg';

export function Dashboard() {
  return (
    <div className="app-container">
      <Navbar userProfile={{ nome: 'Administrador' }} />

      <main className="dashboard-container">
        <h1 className="dashboard-title">Dashboard</h1>

        {/* 4 Cards Superiores */}
        <div className="metrics-grid">
          <div className="metric-card">
            <div className="metric-info">
              <span className="metric-label">Empréstimos Ativos</span>
              <span className="metric-value">760</span>
            </div>
            <div className="metric-icon-bg icon-blue">
              <img src={icon1} alt="Empréstimos Ativos" width={24} height={24} />
            </div>
          </div>

          <div className="metric-card">
            <div className="metric-info">
              <span className="metric-label">Atrasados</span>
              <span className="metric-value">50</span>
            </div>
            <div className="metric-icon-bg icon-yellow">
              <img src={icon2} alt="Atrasados" width={24} height={24} />
            </div>
          </div>

          <div className="metric-card">
            <div className="metric-info">
              <span className="metric-label">Reservas</span>
              <span className="metric-value">88</span>
            </div>
            <div className="metric-icon-bg icon-green">
              <img src={icon3} alt="Reservas" width={24} height={24} />
            </div>
          </div>

          <div className="metric-card">
            <div className="metric-info">
              <span className="metric-label">Total de Livros</span>
              <span className="metric-value">2040</span>
            </div>
            <div className="metric-icon-bg icon-orange">
              <img src={icon4} alt="Total de Livros" width={24} height={24} />
            </div>
          </div>
        </div>

        {}
        <div className="charts-grid">
          {}
          <div className="chart-card">
            <h2 className="chart-title">Empréstimos por Categoria</h2>
            <div className="donut-chart-wrapper">
              <div className="donut-placeholder">
                <div className="donut-inner"></div>
              </div>
              <div className="chart-legend">
                <div className="legend-item">
                  <span className="legend-color" style={{ backgroundColor: '#2563eb' }}></span>
                  <span>44% Ficção</span>
                </div>
                <div className="legend-item">
                  <span className="legend-color" style={{ backgroundColor: '#60a5fa' }}></span>
                  <span>33% Romance</span>
                </div>
                <div className="legend-item">
                  <span className="legend-color" style={{ backgroundColor: '#a5b4fc' }}></span>
                  <span>23% Autoajuda</span>
                </div>
              </div>
            </div>
          </div>

          {}
          <div className="chart-card">
            <h2 className="chart-title">Livros mais Emprestados</h2>
            <ul className="top-books-list">
              <li>Dom Casmurro de Machado de Assis</li>
              <li>1984 de George Orwell</li>
              <li>O Pequeno Príncipe de Antoine de Saint-Exupéry</li>
              <li>Harry Potter e a Pedra Filosofal de J. K. Rowling</li>
              <li>A Metamorfose de Franz Kafka</li>
              <li>O Senhor dos Anéis: A Sociedade do Anel de J. R. Tolkien</li>
              <li>Cem Anos de Solidão de Gabriel García Márquez</li>
            </ul>
          </div>
        </div>
      </main>
    </div>
  );
}