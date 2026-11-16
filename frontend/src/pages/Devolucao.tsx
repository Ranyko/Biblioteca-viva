import { useEffect, useMemo, useState } from 'react';
import { Navbar } from '../components/Navbar';
import { getUsuario, textoErro } from '../services/api';
import { circulacaoService, type Devolucao as DevolucaoRegistrada, type Emprestimo } from '../services/circulacao';
import { dataBR, dataHoraBR, diasDeAtraso, hojeBR, moeda } from '../services/formato';
import { leitoresService, type Leitor } from '../services/leitores';
import './Devolucao.css';

const rotuloLeitor = (l: Leitor) => `${l.nome} (${l.documento})`;
const rotuloEmprestimo = (e: Emprestimo) =>
  `Exemplar #${e.exemplarId} — retirado em ${dataHoraBR(e.dataRetirada)}`;

export function Devolucao() {
  const [leitores, setLeitores] = useState<Leitor[]>([]);
  const [emprestimos, setEmprestimos] = useState<Emprestimo[]>([]);
  const [leitor, setLeitor] = useState('');
  const [livro, setLivro] = useState('');
  const [resultado, setResultado] = useState<DevolucaoRegistrada | null>(null);
  const [erro, setErro] = useState('');
  const [salvando, setSalvando] = useState(false);

  useEffect(() => {
    let cancelado = false;
    (async () => {
      try {
        const [ls, es] = await Promise.all([leitoresService.listar(), circulacaoService.listarAtivos()]);
        if (cancelado) return;
        setLeitores(ls);
        setEmprestimos(es);
      } catch (err) {
        if (!cancelado) setErro(textoErro(err));
      }
    })();
    return () => {
      cancelado = true;
    };
  }, []);

  // Só leitores com empréstimo em aberto aparecem como sugestão.
  const opcoesLeitor = useMemo(() => {
    const comEmprestimo = new Set(emprestimos.map((e) => e.leitorId));
    return leitores
      .filter((l) => comEmprestimo.has(l.id))
      .map((l) => ({ leitor: l, rotulo: rotuloLeitor(l) }));
  }, [leitores, emprestimos]);

  const leitorEscolhido = opcoesLeitor.find((o) => o.rotulo === leitor.trim())?.leitor;
  const emprestimosDoLeitor = useMemo(
    () => (leitorEscolhido ? emprestimos.filter((e) => e.leitorId === leitorEscolhido.id) : []),
    [emprestimos, leitorEscolhido],
  );
  const emprestimoEscolhido = emprestimosDoLeitor.find((e) => rotuloEmprestimo(e) === livro.trim());

  const handleLeitorChange = (valor: string) => {
    setLeitor(valor);
    const o = opcoesLeitor.find((x) => x.rotulo === valor.trim());
    const dele = o ? emprestimos.filter((e) => e.leitorId === o.leitor.id) : [];

    setLivro(dele.length === 1 ? rotuloEmprestimo(dele[0]) : '');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErro('');
    setResultado(null);
    if (!emprestimoEscolhido) {
      setErro('Selecione o leitor e o exemplar a devolver entre as sugestões.');
      return;
    }
    setSalvando(true);
    try {
      const r = await circulacaoService.devolver(emprestimoEscolhido.id);
      setResultado(r);
      setEmprestimos((l) => l.filter((x) => x.id !== emprestimoEscolhido.id));
      setLeitor('');
      setLivro('');
    } catch (err) {
      setErro(textoErro(err));
    } finally {
      setSalvando(false);
    }
  };

  const limpar = () => {
    setLeitor('');
    setLivro('');
    setErro('');
    setResultado(null);
  };

  const diasAtraso = emprestimoEscolhido ? diasDeAtraso(emprestimoEscolhido.dataPrevistaDevolucao) : 0;

  return (
    <div className="app-container">
      <Navbar userProfile={{ nome: getUsuario()?.nome ?? 'Usuário' }} />

      <main className="devolucao-container">
        <h1 className="page-title">Registro de Devolução</h1>

        <div className="devolucao-card">
          {erro && <p style={{ color: '#b91c1c', margin: '0 0 1rem' }}>{erro}</p>}
          {resultado && (
            <div style={{ color: '#047857', margin: '0 0 1rem' }}>
              <p style={{ margin: 0 }}>
                Devolução do empréstimo #{resultado.emprestimo.id} registrada.
                {' '}
                {resultado.statusExemplar === 'reservado'
                  ? 'O exemplar foi reservado para o próximo da fila.'
                  : 'O exemplar voltou a ficar disponível.'}
              </p>
              {resultado.diasAtraso > 0 && (
                <p style={{ margin: '0.25rem 0 0' }}>
                  Atraso de {resultado.diasAtraso} dia(s). Multa: {moeda(resultado.valorMulta)}.
                </p>
              )}
            </div>
          )}
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
                      list="leitores-com-emprestimo"
                      placeholder="Digite o nome ou documento do leitor"
                      value={leitor}
                      onChange={(e) => handleLeitorChange(e.target.value)}
                      autoComplete="off"
                      required
                    />
                    <datalist id="leitores-com-emprestimo">
                      {opcoesLeitor.map((o) => (
                        <option key={o.leitor.id} value={o.rotulo} />
                      ))}
                    </datalist>
                    <svg className="search-icon-svg" viewBox="0 0 24 24" fill="none" stroke="#6B7280" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                      <circle cx="11" cy="11" r="8"></circle>
                      <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                    </svg>
                  </div>
                </div>

                {/* Campo Exemplar (o atendente ainda não consegue consultar o título pela API) */}
                <div className="form-field">
                  <label id='livro' className="field-label" htmlFor="livro">Exemplar</label>
                  <div className="search-input-wrapper">
                    <input
                      id="livro"
                      type="text"
                      list="emprestimos-do-leitor"
                      placeholder="Selecione o exemplar emprestado"
                      value={livro}
                      onChange={(e) => setLivro(e.target.value)}
                      autoComplete="off"
                      required
                    />
                    <datalist id="emprestimos-do-leitor">
                      {emprestimosDoLeitor.map((emp) => (
                        <option key={emp.id} value={rotuloEmprestimo(emp)} />
                      ))}
                    </datalist>
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
                    {emprestimoEscolhido ? dataHoraBR(emprestimoEscolhido.dataRetirada) : '--/--/----'}
                  </div>
                </div>

                {/* Data de Devolução */}
                <div className="form-field">
                  <label id='data-devolucao' className="field-label">Data de Devolução</label>
                  <div className="date-display-box">
                    {hojeBR()}
                  </div>
                </div>

              </div>

              
              {emprestimoEscolhido && (
                <div className="atraso-info-card">
                  <p className="atraso-text">
                    <strong>Devolução prevista:</strong> {dataBR(emprestimoEscolhido.dataPrevistaDevolucao)}
                  </p>
                  {diasAtraso > 0 ? (
                    <>
                      <p className="atraso-text"><strong>Dias de atraso:</strong> {diasAtraso}</p>
                      <p className="multa-text"><strong>Multa:</strong> calculada ao registrar</p>
                    </>
                  ) : (
                    <p className="atraso-text">Sem atraso.</p>
                  )}
                </div>
              )}

            </div>

            {/* Botões de Ação */}
            <div className="action-buttons">
              <button type="button" className="btn-cancel-outline" onClick={limpar}>
                Cancelar
              </button>
              <button type="submit" className="btn-submit-blue" disabled={salvando}>
                {salvando ? 'Registrando...' : 'Registrar Devolução'}
              </button>
            </div>

          </form>
        </div>
      </main>
    </div>
  );
}