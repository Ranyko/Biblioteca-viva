import { useEffect, useState } from 'react';
import { Navbar } from '../components/Navbar';
import { getUsuario, textoErro } from '../services/api';
import { situacaoService, type SituacaoLeitor } from '../services/situacao';
import { dataBR, dataHoraBR, diasDeAtraso, moeda } from '../services/formato';
import { renovacoesService, type Renovacao } from '../services/renovacoes';
import './Avisos.css';
import './MinhaSituacao.css';

const ROTULO_RESERVA: Record<string, string> = {
  ativa: 'Na fila',
  disponivel: 'Disponível para retirada',
  atendida: 'Atendida',
  cancelada: 'Cancelada',
  expirada: 'Expirada',
};

const ROTULO_MULTA: Record<string, string> = {
  pendente: 'Pendente',
  quitada: 'Quitada',
  cancelada: 'Cancelada',
};

export function MinhaSituacao() {
  const [dados, setDados] = useState<SituacaoLeitor | null>(null);
  const [erro, setErro] = useState('');
  const [erroRenovacao, setErroRenovacao] = useState('');
  const [renovandoId, setRenovandoId] = useState<number | null>(null);
  const [historicoAbertoId, setHistoricoAbertoId] = useState<number | null>(null);
  const [historicos, setHistoricos] = useState<Record<number, Renovacao[]>>({});
  const [carregandoHistoricoId, setCarregandoHistoricoId] = useState<number | null>(null);
  const [errosHistorico, setErrosHistorico] = useState<Record<number, string>>({});
  const [sucessosRenovacao, setSucessosRenovacao] = useState<Record<number, string>>({});

  useEffect(() => {
    let cancelado = false;
    (async () => {
      try {
        const r = await situacaoService.consultar();
        if (!cancelado) setDados(r);
      } catch (err) {
        if (!cancelado) setErro(textoErro(err));
      }
    })();
    return () => {
      cancelado = true;
    };
  }, []);

  const renovar = async (emprestimoId: number) => {
    setErroRenovacao('');
    setSucessosRenovacao((atuais) => ({ ...atuais, [emprestimoId]: '' }));
    setRenovandoId(emprestimoId);
    try {
      const renovacao = await renovacoesService.renovarComoLeitor(emprestimoId);
      setDados((atuais) => atuais && ({
        ...atuais,
        emprestimos: atuais.emprestimos.map((emprestimo) =>
          emprestimo.id === emprestimoId
            ? { ...emprestimo, dataPrevistaDevolucao: renovacao.dataPrevistaNova }
            : emprestimo,
        ),
      }));
      setHistoricos((atuais) => emprestimoId in atuais
        ? { ...atuais, [emprestimoId]: [...atuais[emprestimoId], renovacao] }
        : atuais,
      );
      setSucessosRenovacao((atuais) => ({
        ...atuais,
        [emprestimoId]: `Empréstimo renovado. Nova devolução prevista para ${dataBR(renovacao.dataPrevistaNova)}.`,
      }));
    } catch (err) {
      setErroRenovacao(textoErro(err));
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
    setErrosHistorico((atuais) => ({ ...atuais, [emprestimoId]: '' }));
    try {
      const historico = await renovacoesService.historicoComoLeitor(emprestimoId);
      setHistoricos((atuais) => ({ ...atuais, [emprestimoId]: historico }));
    } catch (err) {
      setErrosHistorico((atuais) => ({ ...atuais, [emprestimoId]: textoErro(err) }));
    } finally {
      setCarregandoHistoricoId(null);
    }
  };

  const pendente = dados?.multas
    .filter((m) => m.situacao === 'pendente')
    .reduce((soma, multa) => soma + multa.valorTotal, 0) ?? 0;

  return (
    <div className="avisos-page">
      <Navbar userProfile={{ nome: getUsuario()?.nome ?? 'Leitor' }} />
      <main className="avisos-main">
        <div className="avisos-header">
          <h1 className="avisos-titulo">Minha situação</h1>
        </div>

        {erro && <p className="avisos-erro">{erro}</p>}
        {!dados && !erro && <p className="avisos-vazio">Carregando...</p>}

        {dados && (
          <>
            <section className="avisos-card">
              <h2>Empréstimos</h2>
              {dados.emprestimos.length === 0 ? (
                <p className="avisos-vazio">Você ainda não tem empréstimos.</p>
              ) : (
                <div className="situacao-tabela-wrap">
                  <table className="avisos-tabela situacao-tabela">
                    <thead>
                      <tr>
                        <th>Obra</th>
                        <th>Retirada</th>
                        <th>Devolução prevista</th>
                        <th>Devolvido em</th>
                        <th>Situação</th>
                        <th>Ações</th>
                      </tr>
                    </thead>
                    <tbody>
                      {dados.emprestimos.map((emprestimo) => {
                        const ativo = emprestimo.dataDevolucao === null;
                        const atraso = ativo ? diasDeAtraso(emprestimo.dataPrevistaDevolucao) : 0;
                        return [
                          <tr key={emprestimo.id}>
                            <td>{emprestimo.livroTitulo}</td>
                            <td>{dataHoraBR(emprestimo.dataRetirada)}</td>
                            <td>{dataBR(emprestimo.dataPrevistaDevolucao)}</td>
                            <td>{emprestimo.dataDevolucao ? dataHoraBR(emprestimo.dataDevolucao) : '—'}</td>
                            <td style={atraso > 0 ? { color: '#b91c1c', fontWeight: 600 } : undefined}>
                              {!ativo
                                ? 'Devolvido'
                                : atraso > 0
                                  ? `Atrasado (${atraso} ${atraso === 1 ? 'dia' : 'dias'})`
                                  : 'Em andamento'}
                            </td>
                            <td className="situacao-acoes">
                              {ativo && (
                                <button
                                  type="button"
                                  className="avisos-btn"
                                  disabled={atraso > 0 || renovandoId === emprestimo.id}
                                  onClick={() => void renovar(emprestimo.id)}
                                >
                                  {renovandoId === emprestimo.id ? 'Renovando...' : 'Renovar'}
                                </button>
                              )}
                              <button
                                type="button"
                                className="avisos-btn sec"
                                onClick={() => void alternarHistorico(emprestimo.id)}
                              >
                                {historicoAbertoId === emprestimo.id ? 'Ocultar histórico' : 'Histórico'}
                              </button>
                            </td>
                          </tr>,
                          ...(historicoAbertoId === emprestimo.id
                            ? [<tr key={`${emprestimo.id}-historico`}>
                                <td colSpan={6}>
                                  {carregandoHistoricoId === emprestimo.id && (
                                    <p className="avisos-vazio">Carregando histórico...</p>
                                  )}
                                  {errosHistorico[emprestimo.id] && (
                                    <p className="avisos-erro">{errosHistorico[emprestimo.id]}</p>
                                  )}
                                  {historicos[emprestimo.id] && (
                                    historicos[emprestimo.id].length === 0
                                      ? <p className="avisos-vazio">Este empréstimo ainda não foi renovado.</p>
                                      : <div className="historico-renovacoes">
                                          {historicos[emprestimo.id].map((renovacao) => (
                                            <p key={renovacao.id}>
                                              Renovação {renovacao.numeroRenovacao}/{renovacao.limiteRenovacoesSnapshot}:{' '}
                                              devolução alterada de {dataBR(renovacao.dataPrevistaAnterior)} para{' '}
                                              {dataBR(renovacao.dataPrevistaNova)} em {dataHoraBR(renovacao.renovadaEm)}.
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
              {erroRenovacao && <p className="avisos-erro" role="alert">{erroRenovacao}</p>}
              {Object.entries(sucessosRenovacao).map(([id, mensagem]) =>
                mensagem ? <p key={id} className="avisos-ok" role="status">{mensagem}</p> : null,
              )}
            </section>

            <section className="avisos-card">
              <h2>Reservas</h2>
              {dados.reservas.length === 0 ? (
                <p className="avisos-vazio">Você não tem reservas.</p>
              ) : (
                <table className="avisos-tabela">
                  <thead>
                    <tr>
                      <th>Obra</th>
                      <th>Situação</th>
                      <th>Posição na fila</th>
                      <th>Retirar até</th>
                    </tr>
                  </thead>
                  <tbody>
                    {dados.reservas.map((reserva) => (
                      <tr key={reserva.id}>
                        <td>{reserva.livroTitulo}</td>
                        <td>{ROTULO_RESERVA[reserva.situacao] ?? reserva.situacao}</td>
                        <td>{reserva.posicaoFila ?? '—'}</td>
                        <td>{reserva.dataLimiteRetirada ? dataHoraBR(reserva.dataLimiteRetirada) : '—'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </section>

            <section className="avisos-card">
              <h2>Multas</h2>
              {dados.multas.length === 0 ? (
                <p className="avisos-vazio">Você não tem multas.</p>
              ) : (
                <>
                  {pendente > 0 && (
                    <p className="avisos-erro">Total pendente: <strong>{moeda(pendente)}</strong></p>
                  )}
                  <table className="avisos-tabela">
                    <thead>
                      <tr>
                        <th>Valor</th>
                        <th>Situação</th>
                        <th>Gerada em</th>
                        <th>Paga em</th>
                      </tr>
                    </thead>
                    <tbody>
                      {dados.multas.map((multa) => (
                        <tr key={multa.id}>
                          <td>{moeda(multa.valorTotal)}</td>
                          <td>{ROTULO_MULTA[multa.situacao] ?? multa.situacao}</td>
                          <td>{dataHoraBR(multa.geradaEm)}</td>
                          <td>{multa.pagaEm ? dataHoraBR(multa.pagaEm) : '—'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </>
              )}
            </section>
          </>
        )}
      </main>
    </div>
  );
}
