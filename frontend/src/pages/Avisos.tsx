import { useEffect, useState } from 'react';
import { Navbar } from '../components/Navbar';
import { getUsuario, textoErro } from '../services/api';
import { avisosService, COR_TIPO, ROTULO_TIPO, type Aviso } from '../services/avisos';
import { dataHoraMinBR } from '../services/formato';
import './Avisos.css';

const TAMANHO = 10;
type Filtro = 'todos' | 'nao-lidos' | 'lidos';

export function Avisos() {
  const [filtro, setFiltro] = useState<Filtro>('todos');
  const [pagina, setPagina] = useState(0);
  const [itens, setItens] = useState<Aviso[]>([]);
  const [total, setTotal] = useState(0);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');
  const [recarregar, setRecarregar] = useState(0);

  useEffect(() => {
    let cancelado = false;
    (async () => {
      try {
        const lido = filtro === 'todos' ? undefined : filtro === 'lidos';
        const r = await avisosService.listar({ lido, pagina, tamanho: TAMANHO });
        if (cancelado) return;
        setItens(r.itens);
        setTotal(r.total);
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
  }, [filtro, pagina, recarregar]);

  const trocarFiltro = (f: Filtro) => {
    setCarregando(true);
    setFiltro(f);
    setPagina(0);
  };

  const marcar = async (a: Aviso) => {
    try {
      await avisosService.marcarLido(a.id);
      setRecarregar((n) => n + 1);
    } catch (err) {
      setErro(textoErro(err));
    }
  };

  const marcarTodos = async () => {
    try {
      await avisosService.marcarTodosLidos();
      setRecarregar((n) => n + 1);
    } catch (err) {
      setErro(textoErro(err));
    }
  };

  const ultimaPagina = Math.max(0, Math.ceil(total / TAMANHO) - 1);

  return (
    <div className="avisos-page">
      <Navbar userProfile={{ nome: getUsuario()?.nome ?? 'Leitor' }} />

      <main className="avisos-main">
        <div className="avisos-header">
          <h1 className="avisos-titulo">Avisos</h1>
          <button type="button" className="avisos-btn sec" onClick={marcarTodos}>
            Marcar todos como lidos
          </button>
        </div>

        <div className="avisos-card">
          <div className="avisos-toolbar">
            <label htmlFor="filtro-avisos">Mostrar:</label>
            <select id="filtro-avisos" value={filtro} onChange={(e) => trocarFiltro(e.target.value as Filtro)}>
              <option value="todos">Todos</option>
              <option value="nao-lidos">Não lidos</option>
              <option value="lidos">Lidos</option>
            </select>
          </div>

          {erro && <p className="avisos-erro">{erro}</p>}

          {carregando ? (
            <p className="avisos-vazio">Carregando...</p>
          ) : itens.length === 0 ? (
            <p className="avisos-vazio">Nenhum aviso para mostrar.</p>
          ) : (
            <ul className="aviso-lista">
              {itens.map((a) => (
                <li key={a.id} className={`aviso-item ${COR_TIPO[a.tipo] ?? 'info'} ${a.lidoEm ? '' : 'nao-lido'}`}>
                  <div className="aviso-corpo">
                    <strong>{a.titulo}</strong>
                    <p>{a.mensagem}</p>
                    <div className="aviso-meta">
                      <span className="aviso-selo">{ROTULO_TIPO[a.tipo] ?? a.tipo}</span>
                      <span>{dataHoraMinBR(a.criadoEm)}</span>
                    </div>
                  </div>
                  {!a.lidoEm && (
                    <button type="button" className="aviso-ler" onClick={() => marcar(a)}>
                      Marcar como lido
                    </button>
                  )}
                </li>
              ))}
            </ul>
          )}

          {total > TAMANHO && (
            <div className="avisos-paginacao">
              <button type="button" className="avisos-btn sec" disabled={pagina === 0} onClick={() => setPagina((p) => p - 1)}>
                Anterior
              </button>
              <span>
                Página {pagina + 1} de {ultimaPagina + 1}
              </span>
              <button type="button" className="avisos-btn sec" disabled={pagina >= ultimaPagina} onClick={() => setPagina((p) => p + 1)}>
                Próxima
              </button>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
