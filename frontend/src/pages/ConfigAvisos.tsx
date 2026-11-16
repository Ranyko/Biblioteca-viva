import { useEffect, useState } from 'react';
import { Navbar } from '../components/Navbar';
import { getUsuario, textoErro } from '../services/api';
import { avisosService, ROTULO_TIPO, type PreferenciasAvisos, type Varredura } from '../services/avisos';
import './Avisos.css';

type Campo = Exclude<keyof PreferenciasAvisos, 'atualizadoEm'>;

const CAMPOS: { campo: Campo; tipo: string; ajuda: string }[] = [
  { campo: 'devidaAmanha', tipo: 'DEVIDA_AMANHA', ajuda: 'Empréstimo que vence amanhã' },
  { campo: 'vencido', tipo: 'VENCIDO', ajuda: 'Lembrete diário de empréstimo vencido' },
  { campo: 'reservaDisponivel', tipo: 'RESERVA_DISPONIVEL', ajuda: 'Exemplar reservado ficou disponível' },
  { campo: 'reservaExpirando', tipo: 'RESERVA_EXPIRANDO', ajuda: 'Prazo de retirada da reserva acaba amanhã' },
  { campo: 'reservaExpirada', tipo: 'RESERVA_EXPIRADA', ajuda: 'Reserva expirou sem retirada' },
  { campo: 'multaGerada', tipo: 'MULTA_GERADA', ajuda: 'Multa gerada na devolução' },
  { campo: 'filaPosicao', tipo: 'FILA_POSICAO', ajuda: 'Posição do leitor na fila diminuiu' },
];

export function ConfigAvisos() {
  const [prefs, setPrefs] = useState<PreferenciasAvisos | null>(null);
  const [erro, setErro] = useState('');
  const [aviso, setAviso] = useState('');
  const [salvando, setSalvando] = useState(false);
  const [varrendo, setVarrendo] = useState(false);
  const [resultado, setResultado] = useState<Varredura | null>(null);

  useEffect(() => {
    let cancelado = false;
    (async () => {
      try {
        const p = await avisosService.preferencias();
        if (!cancelado) setPrefs(p);
      } catch (err) {
        if (!cancelado) setErro(textoErro(err));
      }
    })();
    return () => {
      cancelado = true;
    };
  }, []);

  const alternar = (campo: Campo) => setPrefs((p) => (p ? { ...p, [campo]: !p[campo] } : p));

  const salvar = async () => {
    if (!prefs) return;
    setErro('');
    setAviso('');
    setSalvando(true);
    try {
      setPrefs(await avisosService.salvarPreferencias(prefs));
      setAviso('Preferências salvas.');
    } catch (err) {
      setErro(textoErro(err));
    } finally {
      setSalvando(false);
    }
  };

  const gerarAgora = async () => {
    setErro('');
    setAviso('');
    setVarrendo(true);
    try {
      setResultado(await avisosService.executarVarredura());
    } catch (err) {
      setErro(textoErro(err));
    } finally {
      setVarrendo(false);
    }
  };

  return (
    <div className="avisos-page">
      <Navbar userProfile={{ nome: getUsuario()?.nome ?? 'Administrador' }} />

      <main className="avisos-main">
        <div className="avisos-header">
          <h1 className="avisos-titulo">Configurar avisos</h1>
        </div>

        {erro && <p className="avisos-erro">{erro}</p>}
        {aviso && <p className="avisos-ok">{aviso}</p>}

        <div className="avisos-card">
          <h2>Tipos de aviso ativos</h2>
          {!prefs ? (
            !erro && <p className="avisos-vazio">Carregando...</p>
          ) : (
            <>
              <div className="pref-lista">
                {CAMPOS.map(({ campo, tipo, ajuda }) => (
                  <label key={campo}>
                    <input type="checkbox" checked={prefs[campo]} onChange={() => alternar(campo)} />
                    <span>
                      <strong>{ROTULO_TIPO[tipo]}</strong> — {ajuda}
                    </span>
                  </label>
                ))}
              </div>
              <button type="button" className="avisos-btn" onClick={salvar} disabled={salvando}>
                {salvando ? 'Salvando...' : 'Salvar preferências'}
              </button>
            </>
          )}
        </div>

        <div className="avisos-card">
          <h2>Gerar avisos agora</h2>
          <p className="avisos-vazio" style={{ marginBottom: '1rem' }}>
            Executa a varredura que normalmente roda uma vez por dia. Repetir no mesmo dia não duplica avisos.
          </p>
          <button type="button" className="avisos-btn" onClick={gerarAgora} disabled={varrendo}>
            {varrendo ? 'Gerando...' : 'Gerar avisos agora'}
          </button>
          {resultado && (
            <p className="avisos-ok" style={{ marginTop: '1rem' }}>
              {resultado.total} aviso(s) criado(s): devolução amanhã {resultado.devidaAmanha}, vencidos {resultado.vencido}, reserva
              expirando {resultado.reservaExpirando}, reserva expirada {resultado.reservaExpirada}, fila {resultado.filaPosicao}.
            </p>
          )}
        </div>
      </main>
    </div>
  );
}
