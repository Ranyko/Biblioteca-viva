import { useEffect, useState } from 'react';
import { Navbar } from '../components/Navbar';
import { getUsuario, textoErro } from '../services/api';
import { exemplaresService, livrosService } from '../services/catalogo';
import { painelService, type PainelBiblioteca } from '../services/painel';
import { baixarMovimentacoesCsv } from '../services/relatorios';
import './Dashboard.css';

import icon1 from '../assets/icone-dashboard2.svg';
import icon2 from '../assets/icone-dashboard4.svg';
import icon3 from '../assets/icone-dashboard3.svg';
import icon4 from '../assets/icone-dashboard1.svg';

const CORES = ['#2563eb', '#60a5fa', '#a5b4fc', '#34d399', '#fbbf24', '#f87171', '#9ca3af'];
const MAX_FATIAS = 6; // as demais categorias viram "Outras"

interface Totais {
  livros: number;
  exemplares: number;
}

/** Junta as categorias menores em "Outras" e calcula o percentual de cada fatia. */
function fatiasDoGrafico(painel: PainelBiblioteca) {
  const ordenadas = [...painel.emprestimosPorCategoria].sort((a, b) => b.totalEmprestimos - a.totalEmprestimos);
  const principais = ordenadas.slice(0, MAX_FATIAS).map((c) => ({ nome: c.categoria, total: c.totalEmprestimos }));
  const resto = ordenadas.slice(MAX_FATIAS).reduce((soma, c) => soma + c.totalEmprestimos, 0);
  if (resto > 0) principais.push({ nome: 'Outras', total: resto });
  const soma = principais.reduce((s, f) => s + f.total, 0);
  return principais.map((f, i) => {
    const antes = principais.slice(0, i).reduce((s, x) => s + x.total, 0);
    const inicio = soma ? (antes / soma) * 100 : 0;
    const pct = soma ? (f.total / soma) * 100 : 0;
    return { ...f, cor: CORES[i % CORES.length], pct, inicio, fim: inicio + pct };
  });
}

export function Dashboard() {
  const [painel, setPainel] = useState<PainelBiblioteca | null>(null);
  const [totais, setTotais] = useState<Totais | null>(null);
  const [erro, setErro] = useState('');
  const [dataInicio, setDataInicio] = useState('');
  const [dataFim, setDataFim] = useState('');
  const [exportando, setExportando] = useState(false);
  const [erroExport, setErroExport] = useState('');

  useEffect(() => {
    let cancelado = false;
    (async () => {
      try {
        const p = await painelService.consultar();
        if (!cancelado) setPainel(p);
      } catch (err) {
        if (!cancelado) setErro(textoErro(err));
      }
      try {
        const [livros, exemplares] = await Promise.all([livrosService.listar(0, 1), exemplaresService.listar(0, 1)]);
        if (!cancelado) setTotais({ livros: livros.total, exemplares: exemplares.total });
      } catch {
        // os dois totais são complementares; o painel principal já tem seu próprio erro
      }
    })();
    return () => {
      cancelado = true;
    };
  }, []);

  const exportar = async () => {
    setErroExport('');
    if (dataInicio && dataFim && dataInicio > dataFim) {
      setErroExport('A data inicial não pode ser posterior à data final.');
      return;
    }
    setExportando(true);
    try {
      await baixarMovimentacoesCsv(dataInicio || undefined, dataFim || undefined);
    } catch (err) {
      setErroExport(textoErro(err));
    } finally {
      setExportando(false);
    }
  };

  const fatias = painel ? fatiasDoGrafico(painel) : [];
  const gradiente = fatias.map((f) => `${f.cor} ${f.inicio}% ${f.fim}%`).join(', ');

  const valor = (n: number | undefined) => (n === undefined ? '—' : n.toLocaleString('pt-BR'));

  return (
    <div className="app-container">
      <Navbar userProfile={{ nome: getUsuario()?.nome ?? 'Administrador' }} />

      <main className="dashboard-container">
        <h1 className="dashboard-title">Dashboard</h1>

        {erro && <p style={{ color: '#b91c1c', textAlign: 'center', marginBottom: '1rem' }}>{erro}</p>}

        <div className="metrics-grid">
          <div className="metric-card">
            <div className="metric-info">
              <span className="metric-label">Empréstimos Ativos</span>
              <span className="metric-value">{valor(painel?.emprestimosAtivos)}</span>
            </div>
            <div className="metric-icon-bg icon-blue">
              <img src={icon1} alt="Empréstimos Ativos" width={24} height={24} />
            </div>
          </div>

          <div className="metric-card">
            <div className="metric-info">
              <span className="metric-label">Atrasados</span>
              <span className="metric-value">{valor(painel?.emprestimosAtrasados)}</span>
            </div>
            <div className="metric-icon-bg icon-yellow">
              <img src={icon2} alt="Atrasados" width={24} height={24} />
            </div>
          </div>

          <div className="metric-card">
            <div className="metric-info">
              <span className="metric-label">Total de Exemplares</span>
              <span className="metric-value">{valor(totais?.exemplares)}</span>
            </div>
            <div className="metric-icon-bg icon-green">
              <img src={icon3} alt="Total de Exemplares" width={24} height={24} />
            </div>
          </div>

          <div className="metric-card">
            <div className="metric-info">
              <span className="metric-label">Total de Livros</span>
              <span className="metric-value">{valor(totais?.livros)}</span>
            </div>
            <div className="metric-icon-bg icon-orange">
              <img src={icon4} alt="Total de Livros" width={24} height={24} />
            </div>
          </div>
        </div>

        <div className="charts-grid">
          <div className="chart-card">
            <h2 className="chart-title">Empréstimos por Categoria</h2>
            {!painel ? (
              <p style={{ textAlign: 'center', color: '#6b7280' }}>{erro ? '' : 'Carregando...'}</p>
            ) : fatias.length === 0 ? (
              <p style={{ textAlign: 'center', color: '#6b7280' }}>Nenhum empréstimo registrado.</p>
            ) : (
              <div className="donut-chart-wrapper">
                <div className="donut-placeholder" style={{ background: `conic-gradient(${gradiente})` }}>
                  <div className="donut-inner"></div>
                </div>
                <div className="chart-legend">
                  {fatias.map((f) => (
                    <div className="legend-item" key={f.nome}>
                      <span className="legend-color" style={{ backgroundColor: f.cor }}></span>
                      <span>
                        {Math.round(f.pct)}% {f.nome} ({f.total})
                      </span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          <div className="chart-card">
            <h2 className="chart-title">Livros mais Emprestados</h2>
            {!painel ? (
              <p style={{ textAlign: 'center', color: '#6b7280' }}>{erro ? '' : 'Carregando...'}</p>
            ) : painel.rankingObras.length === 0 ? (
              <p style={{ textAlign: 'center', color: '#6b7280' }}>Nenhum empréstimo registrado.</p>
            ) : (
              <ol className="top-books-list">
                {painel.rankingObras.map((o) => (
                  <li key={o.livroId}>
                    {o.titulo} — {o.totalEmprestimos} {o.totalEmprestimos === 1 ? 'empréstimo' : 'empréstimos'}
                  </li>
                ))}
              </ol>
            )}
          </div>
        </div>

        <div className="chart-card export-card">
          <h2 className="chart-title">Exportar movimentações (CSV)</h2>
          <div className="export-form">
            <label>
              De
              <input type="date" value={dataInicio} max={dataFim || undefined} onChange={(e) => setDataInicio(e.target.value)} />
            </label>
            <label>
              Até
              <input type="date" value={dataFim} min={dataInicio || undefined} onChange={(e) => setDataFim(e.target.value)} />
            </label>
            <button type="button" className="export-btn" onClick={exportar} disabled={exportando}>
              {exportando ? 'Gerando...' : 'Baixar CSV'}
            </button>
          </div>
          <p className="export-ajuda">Deixe as datas em branco para exportar todos os períodos.</p>
          {erroExport && <p style={{ color: '#b91c1c', margin: '0.5rem 0 0' }}>{erroExport}</p>}
        </div>
      </main>
    </div>
  );
}
