import { useEffect, useState } from 'react';
import { textoErro } from '../services/api';
import { avaliacoesService, RESENHA_MAX, type Avaliacao, type AvaliacoesLivro } from '../services/avaliacoes';
import { dataBR } from '../services/formato';
import './Avaliacoes.css';

const TAMANHO = 5;

function Estrelas({ nota }: { nota: number }) {
  return (
    <span className="aval-estrelas" aria-label={`Nota ${nota} de 5`}>
      {[1, 2, 3, 4, 5].map((n) => (
        <span key={n} className={n <= nota ? '' : 'off'}>★</span>
      ))}
    </span>
  );
}

/** Avaliações de uma obra: média, resenhas recentes e o formulário do próprio leitor. */
export function Avaliacoes({ livroId }: { livroId: number }) {
  const [dados, setDados] = useState<AvaliacoesLivro | null>(null);
  const [pagina, setPagina] = useState(0);
  const [propria, setPropria] = useState<Avaliacao | null>(null);
  const [nota, setNota] = useState(0);
  const [resenha, setResenha] = useState('');
  const [erroLista, setErroLista] = useState('');
  const [erro, setErro] = useState('');
  const [ok, setOk] = useState('');
  const [salvando, setSalvando] = useState(false);
  const [recarregar, setRecarregar] = useState(0);

  useEffect(() => {
    let cancelado = false;
    (async () => {
      try {
        const r = await avaliacoesService.listar(livroId, pagina, TAMANHO);
        if (cancelado) return;
        setDados(r);
        setErroLista('');
      } catch (err) {
        if (!cancelado) setErroLista(textoErro(err));
      }
    })();
    return () => {
      cancelado = true;
    };
  }, [livroId, pagina, recarregar]);

  useEffect(() => {
    let cancelado = false;
    (async () => {
      try {
        const p = await avaliacoesService.buscarPropria(livroId);
        if (cancelado) return;
        setPropria(p);
        setNota(p?.nota ?? 0);
        setResenha(p?.resenha ?? '');
      } catch {
        // sem a avaliação própria o formulário simplesmente abre vazio
      }
    })();
    return () => {
      cancelado = true;
    };
  }, [livroId, recarregar]);

  const salvar = async () => {
    setErro('');
    setOk('');
    if (nota < 1) {
      setErro('Escolha uma nota de 1 a 5.');
      return;
    }
    setSalvando(true);
    try {
      const corpo = { nota, resenha: resenha.trim() || null };
      if (propria) await avaliacoesService.atualizar(livroId, corpo);
      else await avaliacoesService.criar(livroId, corpo);
      setOk(propria ? 'Avaliação atualizada.' : 'Avaliação publicada.');
      setPagina(0);
      setRecarregar((n) => n + 1);
    } catch (err) {
      setErro(textoErro(err));
    } finally {
      setSalvando(false);
    }
  };

  const excluir = async () => {
    if (!window.confirm('Excluir sua avaliação desta obra?')) return;
    setErro('');
    setOk('');
    setSalvando(true);
    try {
      await avaliacoesService.excluir(livroId);
      setOk('Avaliação excluída.');
      setPagina(0);
      setRecarregar((n) => n + 1);
    } catch (err) {
      setErro(textoErro(err));
    } finally {
      setSalvando(false);
    }
  };

  const ultimaPagina = dados ? Math.max(0, Math.ceil(dados.total / TAMANHO) - 1) : 0;

  return (
    <section className="aval-card">
      <h2>Avaliações</h2>

      {erroLista && <p className="aval-erro">{erroLista}</p>}

      {dados && (
        <>
          {dados.total > 0 ? (
            <div className="aval-resumo">
              <span className="aval-media">{Number(dados.media ?? 0).toLocaleString('pt-BR', { minimumFractionDigits: 1, maximumFractionDigits: 1 })}</span>
              <Estrelas nota={Math.round(Number(dados.media ?? 0))} />
              <span>
                {dados.total} {dados.total === 1 ? 'avaliação' : 'avaliações'}
              </span>
            </div>
          ) : (
            <p className="aval-vazio">Esta obra ainda não tem avaliações.</p>
          )}

          <ul className="aval-lista">
            {dados.itens.map((a) => (
              <li key={a.id} className="aval-item">
                <Estrelas nota={a.nota} />
                <span className="aval-data">{dataBR(a.atualizadaEm.slice(0, 10))}</span>
                {a.resenha && <p>{a.resenha}</p>}
              </li>
            ))}
          </ul>

          {dados.total > TAMANHO && (
            <div className="aval-paginacao">
              <button type="button" className="aval-btn sec" disabled={pagina === 0} onClick={() => setPagina((p) => p - 1)}>
                Anterior
              </button>
              <span>
                Página {pagina + 1} de {ultimaPagina + 1}
              </span>
              <button type="button" className="aval-btn sec" disabled={pagina >= ultimaPagina} onClick={() => setPagina((p) => p + 1)}>
                Próxima
              </button>
            </div>
          )}
        </>
      )}

      <div className="aval-form">
        <h3>{propria ? 'Sua avaliação' : 'Avaliar esta obra'}</h3>
        {erro && <p className="aval-erro">{erro}</p>}
        {ok && <p className="aval-ok">{ok}</p>}

        <div className="aval-picker" role="radiogroup" aria-label="Nota de 1 a 5">
          {[1, 2, 3, 4, 5].map((n) => (
            <button
              key={n}
              type="button"
              role="radio"
              aria-checked={nota === n}
              aria-label={`${n} ${n === 1 ? 'estrela' : 'estrelas'}`}
              className={n <= nota ? 'on' : ''}
              onClick={() => setNota(n)}
            >
              ★
            </button>
          ))}
        </div>

        <textarea
          value={resenha}
          maxLength={RESENHA_MAX}
          placeholder="Escreva uma resenha curta (opcional)"
          onChange={(e) => setResenha(e.target.value)}
        />
        <p className="aval-contador">
          {resenha.length}/{RESENHA_MAX}
        </p>

        <div className="aval-acoes">
          <button type="button" className="aval-btn" onClick={salvar} disabled={salvando}>
            {salvando ? 'Salvando...' : propria ? 'Atualizar avaliação' : 'Publicar avaliação'}
          </button>
          {propria && (
            <button type="button" className="aval-btn perigo" onClick={excluir} disabled={salvando}>
              Excluir avaliação
            </button>
          )}
        </div>
      </div>
    </section>
  );
}
