import { useEffect, useMemo, useState } from 'react';
import { Navbar } from '../components/Navbar';
import { getUsuario, textoErro } from '../services/api';
import { circulacaoService, type Emprestimo as EmprestimoRegistrado } from '../services/circulacao';
import { dataBR, dataHoraBR, hojeBR } from '../services/formato';
import { leitoresService, type Leitor } from '../services/leitores';
import './Emprestimo.css';

const rotuloLeitor = (l: Leitor) => `${l.nome} (${l.documento})`;

export function Emprestimo() {
  const [leitores, setLeitores] = useState<Leitor[]>([]);
  const [leitor, setLeitor] = useState('');
  const [exemplar, setExemplar] = useState('');
  const [registrado, setRegistrado] = useState<EmprestimoRegistrado | null>(null);
  const [erro, setErro] = useState('');
  const [salvando, setSalvando] = useState(false);

  useEffect(() => {
    let cancelado = false;
    (async () => {
      try {
        const lista = await leitoresService.listar();
        if (!cancelado) setLeitores(lista.filter((l) => l.ativo)); // só leitor ativo pode retirar
      } catch (err) {
        if (!cancelado) setErro(textoErro(err));
      }
    })();
    return () => {
      cancelado = true;
    };
  }, []);

  const opcoes = useMemo(() => leitores.map((l) => ({ leitor: l, rotulo: rotuloLeitor(l) })), [leitores]);

  const limpar = () => {
    setLeitor('');
    setExemplar('');
    setErro('');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErro('');
    setRegistrado(null);
    const escolhido = opcoes.find((o) => o.rotulo === leitor.trim());
    if (!escolhido) {
      setErro('Selecione um leitor da lista de sugestões.');
      return;
    }
    const exemplarId = Number(exemplar);
    if (!Number.isInteger(exemplarId) || exemplarId <= 0) {
      setErro('Informe o ID do exemplar (número positivo).');
      return;
    }
    setSalvando(true);
    try {
      setRegistrado(await circulacaoService.emprestar(escolhido.leitor.id, exemplarId));
      limpar();
    } catch (err) {
      setErro(textoErro(err));
    } finally {
      setSalvando(false);
    }
  };

  // Retirada: hoje (o servidor registra a data oficial). Devolução: só o servidor sabe o prazo.
  const retirada = (registrado ? dataHoraBR(registrado.dataRetirada) : hojeBR()).split('/');
  const devolucao = (registrado ? dataBR(registrado.dataPrevistaDevolucao) : '--/--/----').split('/');

  return (
    <div className="app-container">
      <Navbar userProfile={{ nome: getUsuario()?.nome ?? 'Usuário' }} />

      <main className="emprestimo-container">
        <h1 className="page-title">Registro de Empréstimo</h1>

        <div className="emprestimo-card">
          {erro && <p style={{ color: '#b91c1c', margin: '0 0 1rem' }}>{erro}</p>}
          {registrado && (
            <p style={{ color: '#047857', margin: '0 0 1rem' }}>
              Empréstimo #{registrado.id} registrado. Devolução prevista para{' '}
              {dataBR(registrado.dataPrevistaDevolucao)}.
            </p>
          )}
          <form className="emprestimo-form" onSubmit={handleSubmit}>
            
            {/* Campo Leitor */}
            <div className="form-field">
              <label className="field-label" htmlFor="leitor">Leitor</label>
              <div className="search-input-wrapper">
                <input
                  id="leitor"
                  type="text"
                  list="leitores-ativos"
                  placeholder="Digite o nome ou documento do leitor"
                  value={leitor}
                  onChange={(e) => setLeitor(e.target.value)}
                  autoComplete="off"
                  required
                />
                <datalist id="leitores-ativos">
                  {opcoes.map((o) => (
                    <option key={o.leitor.id} value={o.rotulo} />
                  ))}
                </datalist>
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
                  type="number"
                  min={1}
                  placeholder="Digite o ID do exemplar"
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
                <input className="date-input" type="text" value={retirada[0]} readOnly />
                <input className="date-input" type="text" value={retirada[1]} readOnly />
                <input className="date-input" type="text" value={retirada[2]} readOnly />
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
                  {registrado ? 'Preenchida automaticamente' : 'Definida pelo sistema ao registrar'}
                </span>
              </div>
              <div className="date-inputs-grid">
                <input className="date-input" type="text" value={devolucao[0]} readOnly />
                <input className="date-input" type="text" value={devolucao[1]} readOnly />
                <input className="date-input" type="text" value={devolucao[2]} readOnly />
              </div>
            </div>

            {/* Botões */}
            <div className="action-buttons">
              <button type="button" className="btn-cancel-outline" onClick={() => { limpar(); setRegistrado(null); }}>
                Cancelar
              </button>
              <button type="submit" className="btn-submit-blue" disabled={salvando}>
                {salvando ? 'Registrando...' : 'Registrar Empréstimo'}
              </button>
            </div>

          </form>
        </div>
      </main>
    </div>
  );
}