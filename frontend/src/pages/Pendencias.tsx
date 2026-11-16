import { useEffect, useState } from 'react';
import { Navbar } from '../components/Navbar';
import { getUsuario, textoErro } from '../services/api';
import { avisosService, type PendenciasAtendente } from '../services/avisos';
import { dataBR, dataHoraMinBR } from '../services/formato';
import './Avisos.css';

export function Pendencias() {
  const [dados, setDados] = useState<PendenciasAtendente | null>(null);
  const [erro, setErro] = useState('');
  const [recarregar, setRecarregar] = useState(0);

  useEffect(() => {
    let cancelado = false;
    (async () => {
      try {
        const r = await avisosService.pendencias();
        if (cancelado) return;
        setDados(r);
        setErro('');
      } catch (err) {
        if (!cancelado) setErro(textoErro(err));
      }
    })();
    return () => {
      cancelado = true;
    };
  }, [recarregar]);

  return (
    <div className="avisos-page">
      <Navbar userProfile={{ nome: getUsuario()?.nome ?? 'Atendente' }} />

      <main className="avisos-main">
        <div className="avisos-header">
          <h1 className="avisos-titulo">Pendências do dia</h1>
          <button type="button" className="avisos-btn sec" onClick={() => setRecarregar((n) => n + 1)}>
            Atualizar
          </button>
        </div>

        {erro && <p className="avisos-erro">{erro}</p>}
        {!dados && !erro && <p className="avisos-vazio">Carregando...</p>}

        {dados && (
          <>
            <div className="avisos-card">
              <h2>Empréstimos atrasados ({dados.atrasados.length})</h2>
              {dados.atrasados.length === 0 ? (
                <p className="avisos-vazio">Nenhum empréstimo atrasado.</p>
              ) : (
                <table className="avisos-tabela">
                  <thead>
                    <tr>
                      <th>Leitor</th>
                      <th>Obra</th>
                      <th>Devolução prevista</th>
                      <th>Dias de atraso</th>
                    </tr>
                  </thead>
                  <tbody>
                    {dados.atrasados.map((a) => (
                      <tr key={a.emprestimoId}>
                        <td>{a.leitorNome}</td>
                        <td>{a.livroTitulo}</td>
                        <td>{dataBR(a.dataPrevistaDevolucao)}</td>
                        <td>{a.diasAtraso}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>

            <div className="avisos-card">
              <h2>Reservas com retirada vencendo ({dados.reservasExpirando.length})</h2>
              {dados.reservasExpirando.length === 0 ? (
                <p className="avisos-vazio">Nenhuma reserva perto do prazo.</p>
              ) : (
                <table className="avisos-tabela">
                  <thead>
                    <tr>
                      <th>Leitor</th>
                      <th>Obra</th>
                      <th>Retirar até</th>
                    </tr>
                  </thead>
                  <tbody>
                    {dados.reservasExpirando.map((r) => (
                      <tr key={r.reservaId}>
                        <td>{r.leitorNome}</td>
                        <td>{r.livroTitulo}</td>
                        <td>{dataHoraMinBR(r.dataLimiteRetirada)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </>
        )}
      </main>
    </div>
  );
}
