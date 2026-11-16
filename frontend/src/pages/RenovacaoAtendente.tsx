import { useEffect, useMemo, useState } from 'react';
import { Navbar } from '../components/Navbar';
import { getUsuario, textoErro } from '../services/api';
import { dataBR, dataHoraBR, diasDeAtraso } from '../services/formato';
import { leitoresService, type Leitor } from '../services/leitores';
import { renovacoesService, type Renovacao } from '../services/renovacoes';
import { circulacaoService, type Emprestimo } from '../services/circulacao';
import './Avisos.css';
import './RenovacaoAtendente.css';

const rotuloLeitor = (emprestimo: Emprestimo, leitores: Leitor[]) => {
  const leitor = leitores.find((item) => item.id === emprestimo.leitorId);
  return leitor ? `${leitor.nome} (${leitor.documento})` : `Leitor #${emprestimo.leitorId}`;
};

export function RenovacaoAtendente() {
  const [emprestimos, setEmprestimos] = useState<Emprestimo[]>([]);
  const [leitores, setLeitores] = useState<Leitor[]>([]);
  const [busca, setBusca] = useState('');
  const [erro, setErro] = useState('');
  const [carregando, setCarregando] = useState(true);
  const [renovandoId, setRenovandoId] = useState<number | null>(null);
  const [historicoAbertoId, setHistoricoAbertoId] = useState<number | null>(null);
  const [historicos, setHistoricos] = useState<Record<number, Renovacao[]>>({});
  const [carregandoHistoricoId, setCarregandoHistoricoId] = useState<number | null>(null);
  const [mensagensHistorico, setMensagensHistorico] = useState<Record<number, string>>({});
  const [sucessos, setSucessos] = useState<Record<number, string>>({});

  useEffect(() => {
    let cancelado = false;
    (async () => {
      try {
        const [ativos, listaLeitores] = await Promise.all([
          circulacaoService.listarAtivos(),
          leitoresService.listar(),
        ]);
        if (cancelado) return;
        setEmprestimos(ativos);
        setLeitores(listaLeitores);
        setErro('');
      } catch (err) {
        if (!cancelado) setErro(textoErro(err));
      } finally {
        if (!cancelado) setCarregando(false);
      }
    })();
    return () => {
      cancelado = true;
    };
  }, []);

  const emprestimosFiltrados = useMemo(() => {
    const consulta = busca.trim().toLocaleLowerCase('pt-BR');
    if (!consulta) return emprestimos;
    return emprestimos.filter((emprestimo) =>
      [
        String(emprestimo.id),
        String(emprestimo.exemplarId),
        rotuloLeitor(emprestimo, leitores),
      ].some((campo) => campo.toLocaleLowerCase('pt-BR').includes(consulta)),
    );
  }, [busca, emprestimos, leitores]);

  const renovar = async (emprestimo: Emprestimo) => {
    setErro('');
    setSucessos((atuais) => ({ ...atuais, [emprestimo.id]: '' }));
    setRenovandoId(emprestimo.id);
    try {
      const renovacao = await renovacoesService.renovarComoAtendente(emprestimo.id);
      setEmprestimos((atuais) => atuais.map((item) =>
        item.id === emprestimo.id
          ? { ...item, dataPrevistaDevolucao: renovacao.dataPrevistaNova }
          : item,
      ));
      setHistoricos((atuais) => emprestimo.id in atuais
        ? { ...atuais, [emprestimo.id]: [...atuais[emprestimo.id], renovacao] }
        : atuais,
      );
      setSucessos((atuais) => ({
        ...atuais,
        [emprestimo.id]: `Renovação ${renovacao.numeroRenovacao} realizada. Nova devolução: ${dataBR(renovacao.dataPrevistaNova)}.`,
      }));
    } catch (err) {
      setErro(textoErro(err));
    } finally {
      setRenovandoId(null);
    }
  };

  const alternarHistorico = async (emprestimoId: number) => {
    if (historicoAbertoId === emprestimoId) {
      setHistoricoAbertoId(null);
      return;
    }
    setHistoricoAbertoId(emprestimoId);
    if (historicos[emprestimoId]) return;
    setCarregandoHistoricoId(emprestimoId);
    setMensagensHistorico((atuais) => ({ ...atuais, [emprestimoId]: '' }));
    try {
      const historico = await renovacoesService.historicoComoAtendente(emprestimoId);
      setHistoricos((atuais) => ({ ...atuais, [emprestimoId]: historico }));
    } catch (err) {
      setMensagensHistorico((atuais) => ({ ...atuais, [emprestimoId]: textoErro(err) }));
    } finally {
      setCarregandoHistoricoId(null);
    }
  };

  return (
    <div className="avisos-page">
      <Navbar userProfile={{ nome: getUsuario()?.nome ?? 'Atendente' }} />
      <main className="avisos-main renovacao-main">
        <div className="avisos-header">
          <div>
            <h1 className="avisos-titulo">Renovação de empréstimos</h1>
            <p className="renovacao-subtitulo">Renove empréstimos ativos em nome do leitor e consulte o histórico.</p>
          </div>
          <label className="renovacao-busca">
            <span>Buscar leitor, empréstimo ou exemplar</span>
            <input
              type="search"
              value={busca}
              onChange={(event) => setBusca(event.target.value)}
              placeholder="Nome, documento ou número"
            />
          </label>
        </div>

        {erro && <p className="avisos-erro" role="alert">{erro}</p>}
        {carregando && <p className="avisos-vazio">Carregando empréstimos ativos...</p>}
        {!carregando && !erro && emprestimos.length === 0 && (
          <section className="avisos-card"><p className="avisos-vazio">Não há empréstimos ativos para renovar.</p></section>
        )}
        {!carregando && emprestimos.length > 0 && (
          <section className="avisos-card">
            <div className="renovacao-card-header">
              <h2>Empréstimos ativos</h2>
              <span>{emprestimosFiltrados.length} resultado(s)</span>
            </div>
            {emprestimosFiltrados.length === 0 ? (
              <p className="avisos-vazio">Nenhum empréstimo corresponde à busca.</p>
            ) : (
              <div className="renovacao-tabela-wrap">
                <table className="avisos-tabela renovacao-tabela">
                  <thead>
                    <tr>
                      <th>Leitor</th>
                      <th>Empréstimo / exemplar</th>
                      <th>Retirada</th>
                      <th>Devolução prevista</th>
                      <th>Situação</th>
                      <th>Ações</th>
                    </tr>
                  </thead>
                  <tbody>
                    {emprestimosFiltrados.map((emprestimo) => {
                      const atraso = diasDeAtraso(emprestimo.dataPrevistaDevolucao);
                      return [
                        <tr key={emprestimo.id}>
                          <td>{rotuloLeitor(emprestimo, leitores)}</td>
                          <td>#{emprestimo.id} / exemplar #{emprestimo.exemplarId}</td>
                          <td>{dataHoraBR(emprestimo.dataRetirada)}</td>
                          <td>{dataBR(emprestimo.dataPrevistaDevolucao)}</td>
                          <td className={atraso > 0 ? 'renovacao-atrasado' : undefined}>
                            {atraso > 0 ? `Atrasado há ${atraso} ${atraso === 1 ? 'dia' : 'dias'}` : 'Em andamento'}
                          </td>
                          <td className="renovacao-acoes">
                            <button
                              type="button"
                              className="avisos-btn"
                              disabled={atraso > 0 || renovandoId === emprestimo.id}
                              onClick={() => void renovar(emprestimo)}
                              title={atraso > 0 ? 'Empréstimos vencidos não podem ser renovados.' : undefined}
                            >
                              {renovandoId === emprestimo.id ? 'Renovando...' : 'Renovar'}
                            </button>
                            <button
                              type="button"
                              className="avisos-btn sec"
                              onClick={() => void alternarHistorico(emprestimo.id)}
                            >
                              {historicoAbertoId === emprestimo.id ? 'Ocultar' : 'Histórico'}
                            </button>
                          </td>
                        </tr>,
                        ...(historicoAbertoId === emprestimo.id
                          ? [<tr key={`${emprestimo.id}-historico`}>
                              <td colSpan={6}>
                                {carregandoHistoricoId === emprestimo.id && <p className="avisos-vazio">Carregando histórico...</p>}
                                {mensagensHistorico[emprestimo.id] && (
                                  <p className="avisos-erro">{mensagensHistorico[emprestimo.id]}</p>
                                )}
                                {historicos[emprestimo.id] && (
                                  historicos[emprestimo.id].length === 0
                                    ? <p className="avisos-vazio">Este empréstimo ainda não foi renovado.</p>
                                    : <div className="historico-renovacoes">
                                        {historicos[emprestimo.id].map((renovacao) => (
                                          <p key={renovacao.id}>
                                            Renovação {renovacao.numeroRenovacao}/{renovacao.limiteRenovacoesSnapshot}: devolução alterada de{' '}
                                            {dataBR(renovacao.dataPrevistaAnterior)} para {dataBR(renovacao.dataPrevistaNova)} em{' '}
                                            {dataHoraBR(renovacao.renovadaEm)}; atendente #{renovacao.atendenteRenovacaoId}.
                                          </p>
                                        ))}
                                      </div>
                                )}
                              </td>
                            </tr>]
                          : []),
                      ];
                    })}
                  </tbody>
                </table>
              </div>
            )}
            {Object.entries(sucessos).map(([id, mensagem]) =>
              mensagem ? <p key={id} className="avisos-ok">{mensagem}</p> : null,
            )}
          </section>
        )}
      </main>
    </div>
  );
}
