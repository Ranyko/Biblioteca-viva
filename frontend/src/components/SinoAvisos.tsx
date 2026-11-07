import { useCallback, useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { avisosService, EVENTO_AVISOS, type Aviso } from '../services/avisos';
import './SinoAvisos.css';

const INTERVALO_MS = 60_000;

/** Sino de avisos do leitor: badge de não lidos (atualiza a cada 60 s e a cada troca de tela). */
export function SinoAvisos() {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const [naoLidos, setNaoLidos] = useState(0);
  const [aberto, setAberto] = useState(false);
  const [recentes, setRecentes] = useState<Aviso[]>([]);
  const [carregando, setCarregando] = useState(false);
  const raiz = useRef<HTMLDivElement>(null);

  const atualizarContagem = useCallback(async () => {
    try {
      setNaoLidos(await avisosService.contarNaoLidos());
    } catch {
      // o sino é secundário: falha de rede não deve atrapalhar a tela
    }
  }, []);

  useEffect(() => {
    const inicial = setTimeout(atualizarContagem, 0);
    const timer = setInterval(atualizarContagem, INTERVALO_MS);
    window.addEventListener(EVENTO_AVISOS, atualizarContagem);
    return () => {
      clearTimeout(inicial);
      clearInterval(timer);
      window.removeEventListener(EVENTO_AVISOS, atualizarContagem);
    };
  }, [atualizarContagem, pathname]);

  // Fecha o painel ao clicar fora.
  useEffect(() => {
    if (!aberto) return;
    const fora = (e: MouseEvent) => {
      if (raiz.current && !raiz.current.contains(e.target as Node)) setAberto(false);
    };
    document.addEventListener('mousedown', fora);
    return () => document.removeEventListener('mousedown', fora);
  }, [aberto]);

  const alternar = async () => {
    const vaiAbrir = !aberto;
    setAberto(vaiAbrir);
    if (!vaiAbrir) return;
    setCarregando(true);
    try {
      const pagina = await avisosService.listar({ pagina: 0, tamanho: 5 });
      setRecentes(pagina.itens);
    } catch {
      setRecentes([]);
    } finally {
      setCarregando(false);
    }
  };

  const abrirCentral = () => {
    setAberto(false);
    navigate('/avisos');
  };

  return (
    <div className="sino-wrap" ref={raiz}>
      <button
        type="button"
        className="sino-btn"
        onClick={alternar}
        title="Avisos"
        aria-label={naoLidos > 0 ? `Avisos: ${naoLidos} não lidos` : 'Avisos'}
      >
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.73 21a2 2 0 0 1-3.46 0" />
        </svg>
        {naoLidos > 0 && <span className="sino-badge">{naoLidos > 99 ? '99+' : naoLidos}</span>}
      </button>

      {aberto && (
        <div className="sino-painel">
          <h3>Avisos</h3>
          {carregando ? (
            <p className="sino-vazio">Carregando...</p>
          ) : recentes.length === 0 ? (
            <p className="sino-vazio">Nenhum aviso por enquanto.</p>
          ) : (
            <ul className="sino-lista">
              {recentes.map((a) => (
                <li key={a.id} className={`sino-item ${a.lidoEm ? '' : 'nao-lido'}`} onClick={abrirCentral}>
                  <strong>{a.titulo}</strong>
                  <span>{a.mensagem}</span>
                </li>
              ))}
            </ul>
          )}
          <button type="button" className="sino-rodape" onClick={abrirCentral}>
            Ver todos os avisos
          </button>
        </div>
      )}
    </div>
  );
}
