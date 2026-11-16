import { useEffect, useMemo, useState } from 'react';
import { Navbar } from '../components/Navbar';
import { getUsuario, textoErro } from '../services/api';
import { pesquisarAcervo, type ObraAcervo } from '../services/acervo';
import { leitoresService, type Leitor } from '../services/leitores';
import { reservasService, type ItemFila } from '../services/reservas';
import './FilaReserva.css';

const rotuloObra = (o: ObraAcervo) => `${o.titulo} (${o.isbn})`;
const rotuloLeitor = (l: Leitor) => `${l.nome} (${l.documento})`;

export function FilaReserva() {
  const [textoObra, setTextoObra] = useState('');
  const [obras, setObras] = useState<ObraAcervo[]>([]);
  const [leitores, setLeitores] = useState<Leitor[]>([]);
  const [textoLeitor, setTextoLeitor] = useState('');
  const [fila, setFila] = useState<ItemFila[]>([]);
  const [carregandoFila, setCarregandoFila] = useState(false);
  const [recarregar, setRecarregar] = useState(0);
  const [erro, setErro] = useState('');
  const [aviso, setAviso] = useState('');
  const [salvando, setSalvando] = useState(false);

  const obra = useMemo(() => obras.find((o) => rotuloObra(o) === textoObra) ?? null, [obras, textoObra]);

  // Sugestões de obras (acervo é aberto a qualquer perfil logado).
  useEffect(() => {
    let cancelado = false;
    const timer = setTimeout(async () => {
      try {
        const r = await pesquisarAcervo(textoObra);
        if (!cancelado) setObras(r.obras);
      } catch (err) {
        if (!cancelado) setErro(textoErro(err));
      }
    }, textoObra ? 300 : 0);
    return () => {
      cancelado = true;
      clearTimeout(timer);
    };
  }, [textoObra]);

  useEffect(() => {
    let cancelado = false;
    (async () => {
      try {
        const lista = await leitoresService.listar();
        if (!cancelado) setLeitores(lista.filter((l) => l.ativo));
      } catch (err) {
        if (!cancelado) setErro(textoErro(err));
      }
    })();
    return () => {
      cancelado = true;
    };
  }, []);

  // Fila da obra escolhida.
  const obraId = obra?.id ?? null;
  useEffect(() => {
    let cancelado = false;
    (async () => {
      if (obraId === null) {
        setFila([]);
        return;
      }
      setCarregandoFila(true);
      try {
        const itens = await reservasService.fila(obraId);
        if (!cancelado) setFila(itens);
      } catch (err) {
        if (!cancelado) setErro(textoErro(err));
      } finally {
        if (!cancelado) setCarregandoFila(false);
      }
    })();
    return () => {
      cancelado = true;
    };
  }, [obraId, recarregar]);

  const handleReservar = async (e: React.FormEvent) => {
    e.preventDefault();
    setErro('');
    setAviso('');
    const leitor = leitores.find((l) => rotuloLeitor(l) === textoLeitor.trim());
    if (!obra) return setErro('Escolha uma obra da lista de sugestões.');
    if (!leitor) return setErro('Selecione um leitor da lista de sugestões.');
    setSalvando(true);
    try {
      const r = await reservasService.criar(leitor.id, obra.id);
      setAviso(`Reserva #${r.id} criada para ${r.leitorNome} (posição ${r.posicaoFila}).`);
      setTextoLeitor('');
      setRecarregar((n) => n + 1);
    } catch (err) {
      setErro(textoErro(err));
    } finally {
      setSalvando(false);
    }
  };

  const handleCancelar = async (item: ItemFila) => {
    if (!confirm(`Cancelar a reserva de ${item.leitorNome}?`)) return;
    setErro('');
    setAviso('');
    try {
      await reservasService.cancelar(item.reservaId);
      setAviso(`Reserva de ${item.leitorNome} cancelada.`);
      setRecarregar((n) => n + 1);
    } catch (err) {
      setErro(textoErro(err));
    }
  };

  return (
    <div className="app-container">
      <Navbar userProfile={{ nome: getUsuario()?.nome ?? 'Atendente' }} />

      <main className="reserva-container">
        <h1 className="page-title">Fila de Reserva</h1>

        <div className="reserva-card">
          {erro && <p style={{ color: '#b91c1c', margin: '0 0 1rem' }}>{erro}</p>}
          {aviso && <p style={{ color: '#047857', margin: '0 0 1rem' }}>{aviso}</p>}

          <label htmlFor="obra" style={{ fontWeight: 700, display: 'block', marginBottom: '0.5rem' }}>Obra</label>
          <input
            id="obra"
            type="text"
            list="obras-acervo"
            placeholder="Digite o título, autor ou categoria da obra"
            value={textoObra}
            onChange={(e) => setTextoObra(e.target.value)}
            autoComplete="off"
            style={{ width: '100%', maxWidth: 520, padding: '0.6rem', marginBottom: '1.5rem', boxSizing: 'border-box' }}
          />
          <datalist id="obras-acervo">
            {obras.map((o) => (
              <option key={o.id} value={rotuloObra(o)} />
            ))}
          </datalist>

          {obra && (
            <p style={{ margin: '0 0 1rem' }}>
              Exemplares disponíveis: {obra.exemplaresDisponiveis}
              {obra.exemplaresDisponiveis > 0 && ' (só é possível reservar obra sem exemplar disponível)'}
            </p>
          )}

          <div className="table-responsive">
            <table className="reserva-table">
              <thead>
                <tr>
                  <th>Posição</th>
                  <th>Leitor</th>
                  <th>Status</th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                {fila.map((item) => (
                  <tr key={item.reservaId}>
                    <td className="col-posicao">{item.posicao}</td>
                    <td className="col-leitor">{item.leitorNome}</td>
                    <td className="col-status">Aguardando</td>
                    <td className="col-acoes">
                      <button
                        type="button"
                        className="btn-icon btn-delete"
                        title="Cancelar reserva"
                        onClick={() => handleCancelar(item)}
                      >
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <polyline points="3 6 5 6 21 6" />
                          <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                        </svg>
                      </button>
                    </td>
                  </tr>
                ))}
                {fila.length === 0 && (
                  <tr>
                    <td colSpan={4}>
                      {!obra ? 'Escolha uma obra para ver a fila.' : carregandoFila ? 'Carregando...' : 'Nenhuma reserva na fila desta obra.'}
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>

          {obra && (
            <form onSubmit={handleReservar} style={{ marginTop: '2rem' }}>
              <label htmlFor="leitor-reserva" style={{ fontWeight: 700, display: 'block', marginBottom: '0.5rem' }}>
                Nova reserva para esta obra
              </label>
              <input
                id="leitor-reserva"
                type="text"
                list="leitores-reserva"
                placeholder="Digite o nome ou documento do leitor"
                value={textoLeitor}
                onChange={(e) => setTextoLeitor(e.target.value)}
                autoComplete="off"
                required
                style={{ width: '100%', maxWidth: 520, padding: '0.6rem', marginRight: '0.75rem', boxSizing: 'border-box' }}
              />
              <datalist id="leitores-reserva">
                {leitores.map((l) => (
                  <option key={l.id} value={rotuloLeitor(l)} />
                ))}
              </datalist>
              <button type="submit" className="btn-submit-blue" disabled={salvando}>
                {salvando ? 'Reservando...' : 'Reservar'}
              </button>
            </form>
          )}
        </div>
      </main>
    </div>
  );
}
