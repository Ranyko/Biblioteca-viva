import { useEffect, useState } from 'react';
import { Navbar } from '../components/Navbar';
import { getUsuario, textoErro } from '../services/api';
import { situacaoService, type SituacaoLeitor } from '../services/situacao';
import { dataBR, dataHoraBR, diasDeAtraso, moeda } from '../services/formato';
import './Avisos.css';

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

  const pendente = dados?.multas.filter((m) => m.situacao === 'pendente').reduce((s, m) => s + m.valorTotal, 0) ?? 0;

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
            <div className="avisos-card">
              <h2>Empréstimos</h2>
              {dados.emprestimos.length === 0 ? (
                <p className="avisos-vazio">Você ainda não tem empréstimos.</p>
              ) : (
                <table className="avisos-tabela">
                  <thead>
                    <tr>
                      <th>Obra</th>
                      <th>Retirada</th>
                      <th>Devolução prevista</th>
                      <th>Devolvido em</th>
                      <th>Situação</th>
                    </tr>
                  </thead>
                  <tbody>
                    {dados.emprestimos.map((e) => {
                      const ativo = e.dataDevolucao === null;
                      const atraso = ativo ? diasDeAtraso(e.dataPrevistaDevolucao) : 0;
                      return (
                        <tr key={e.id}>
                          <td>{e.livroTitulo}</td>
                          <td>{dataHoraBR(e.dataRetirada)}</td>
                          <td>{dataBR(e.dataPrevistaDevolucao)}</td>
                          <td>{e.dataDevolucao ? dataHoraBR(e.dataDevolucao) : '—'}</td>
                          <td style={atraso > 0 ? { color: '#b91c1c', fontWeight: 600 } : undefined}>
                            {!ativo ? 'Devolvido' : atraso > 0 ? `Atrasado (${atraso} ${atraso === 1 ? 'dia' : 'dias'})` : 'Em andamento'}
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              )}
            </div>

            <div className="avisos-card">
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
                    {dados.reservas.map((r) => (
                      <tr key={r.id}>
                        <td>{r.livroTitulo}</td>
                        <td>{ROTULO_RESERVA[r.situacao] ?? r.situacao}</td>
                        <td>{r.posicaoFila ?? '—'}</td>
                        <td>{r.dataLimiteRetirada ? dataHoraBR(r.dataLimiteRetirada) : '—'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>

            <div className="avisos-card">
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
                      {dados.multas.map((m) => (
                        <tr key={m.id}>
                          <td>{moeda(m.valorTotal)}</td>
                          <td>{ROTULO_MULTA[m.situacao] ?? m.situacao}</td>
                          <td>{dataHoraBR(m.geradaEm)}</td>
                          <td>{m.pagaEm ? dataHoraBR(m.pagaEm) : '—'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </>
              )}
            </div>
          </>
        )}
      </main>
    </div>
  );
}
