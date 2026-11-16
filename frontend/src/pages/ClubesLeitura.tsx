import { useEffect, useMemo, useState } from 'react';
import { Navbar } from '../components/Navbar';
import { getUsuario, textoErro } from '../services/api';
import {
  clubesLeituraService,
  type ClubeLeitura,
  type EncontroClube,
  type MinhaInscricao,
} from '../services/clubesLeitura';
import { dataHoraMinBR } from '../services/formato';
import './Avisos.css';
import './ClubesLeitura.css';

export function ClubesLeitura() {
  const [clubes, setClubes] = useState<ClubeLeitura[]>([]);
  const [encontros, setEncontros] = useState<EncontroClube[]>([]);
  const [inscricoes, setInscricoes] = useState<MinhaInscricao[]>([]);
  const [historico, setHistorico] = useState<MinhaInscricao[]>([]);
  const [clubeId, setClubeId] = useState<number | null>(null);
  const [busca, setBusca] = useState('');
  const [categoriaId, setCategoriaId] = useState('');
  const [abaInscricoes, setAbaInscricoes] = useState<'proximos' | 'historico'>('proximos');
  const [carregando, setCarregando] = useState(true);
  const [carregandoEncontros, setCarregandoEncontros] = useState(false);
  const [agora, setAgora] = useState<number | null>(null);
  const [erro, setErro] = useState('');
  const [mensagem, setMensagem] = useState('');
  const [processandoId, setProcessandoId] = useState<number | null>(null);

  useEffect(() => {
    const timer = window.setTimeout(() => setAgora(Date.now()), 0);
    const intervalo = window.setInterval(() => setAgora(Date.now()), 60_000);
    return () => {
      window.clearTimeout(timer);
      window.clearInterval(intervalo);
    };
  }, []);

  useEffect(() => {
    let cancelado = false;
    (async () => {
      try {
        const [listaClubes, proximos, passadas] = await Promise.all([
          clubesLeituraService.listarClubesLeitor(),
          clubesLeituraService.minhasInscricoes(false),
          clubesLeituraService.minhasInscricoes(true),
        ]);
        if (cancelado) return;
        setClubes(listaClubes);
        setClubeId(listaClubes[0]?.id ?? null);
        setInscricoes(proximos);
        setHistorico(passadas);
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

  useEffect(() => {
    if (clubeId === null) {
      return;
    }
    let cancelado = false;
    void (async () => {
      setCarregandoEncontros(true);
      setErro('');
      try {
        const lista = await clubesLeituraService.listarEncontrosLeitor(clubeId);
        if (!cancelado) setEncontros(lista);
      } catch (err) {
        if (!cancelado) setErro(textoErro(err));
      } finally {
        if (!cancelado) setCarregandoEncontros(false);
      }
    })();
    return () => {
      cancelado = true;
    };
  }, [clubeId]);

  const categorias = useMemo(() => {
    const porId = new Map<number, string>();
    for (const clube of clubes) {
      for (const categoria of clube.categorias) porId.set(categoria.id, categoria.nome);
    }
    return [...porId].map(([id, nome]) => ({ id, nome })).sort((a, b) => a.nome.localeCompare(b.nome, 'pt-BR'));
  }, [clubes]);

  const clubesFiltrados = useMemo(() => {
    const termo = busca.trim().toLocaleLowerCase('pt-BR');
    return clubes.filter((clube) =>
      (!termo || `${clube.nome} ${clube.descricao ?? ''}`.toLocaleLowerCase('pt-BR').includes(termo))
      && (!categoriaId || clube.categorias.some((categoria) => categoria.id === Number(categoriaId))),
    );
  }, [busca, categoriaId, clubes]);

  const minhasInscricoes = useMemo(() => new Map(
    [
      ...historico.filter((inscricao) => inscricao.statusInscricao === 'cancelada'),
      ...inscricoes,
    ].map((inscricao) => [inscricao.encontroId, inscricao]),
  ), [historico, inscricoes]);

  const selecionarClube = (id: number) => {
    if (id !== clubeId) {
      setEncontros([]);
      setErro('');
    }
    setClubeId(id);
  };

  const atualizarInscricoes = async () => {
    const [proximos, passadas] = await Promise.all([
      clubesLeituraService.minhasInscricoes(false),
      clubesLeituraService.minhasInscricoes(true),
    ]);
    setInscricoes(proximos);
    setHistorico(passadas);
  };

  const inscrever = async (encontro: EncontroClube) => {
    setErro('');
    setMensagem('');
    setProcessandoId(encontro.id);
    try {
      await clubesLeituraService.inscrever(encontro.id);
      await Promise.all([
        atualizarInscricoes(),
        clubesLeituraService.listarEncontrosLeitor(encontro.clubeId).then(setEncontros),
      ]);
      setMensagem(`Inscrição confirmada para ${encontro.livroTitulo}.`);
    } catch (err) {
      setErro(textoErro(err));
    } finally {
      setProcessandoId(null);
    }
  };

  const cancelarInscricao = async (encontro: EncontroClube) => {
    setErro('');
    setMensagem('');
    setProcessandoId(encontro.id);
    try {
      await clubesLeituraService.cancelarInscricao(encontro.id);
      await Promise.all([
        atualizarInscricoes(),
        clubesLeituraService.listarEncontrosLeitor(encontro.clubeId).then(setEncontros),
      ]);
      setMensagem('Inscrição cancelada.');
    } catch (err) {
      setErro(textoErro(err));
    } finally {
      setProcessandoId(null);
    }
  };

  const encontrosVisiveis = encontros.filter((encontro) => encontro.status === 'aberto');
  const inscricoesVisiveis = abaInscricoes === 'proximos' ? inscricoes : historico;

  return (
    <div className="avisos-page">
      <Navbar userProfile={{ nome: getUsuario()?.nome ?? 'Leitor' }} />
      <main className="avisos-main clubes-main">
        <div className="avisos-header">
          <div>
            <h1 className="avisos-titulo">Clubes de leitura</h1>
            <p className="clubes-subtitulo">Encontre encontros, participe e acompanhe suas inscrições.</p>
          </div>
          <div className="clubes-filtros">
            <label>
              Buscar clube
              <input
                type="search"
                value={busca}
                onChange={(event) => setBusca(event.target.value)}
                placeholder="Nome ou descrição"
              />
            </label>
            <label>
              Categoria
              <select value={categoriaId} onChange={(event) => setCategoriaId(event.target.value)}>
                <option value="">Todas</option>
                {categorias.map((categoria) => (
                  <option key={categoria.id} value={categoria.id}>{categoria.nome}</option>
                ))}
              </select>
            </label>
          </div>
        </div>
        {erro && <p className="avisos-erro" role="alert">{erro}</p>}
        {mensagem && <p className="avisos-ok" role="status">{mensagem}</p>}
        {carregando && <p className="avisos-vazio">Carregando clubes e inscrições...</p>}

        {!carregando && (
          <section className="avisos-card">
            <h2>Clubes ativos</h2>
            {clubesFiltrados.length === 0 ? (
              <p className="avisos-vazio">
                {clubes.length === 0 ? 'Não há clubes ativos no momento.' : 'Nenhum clube corresponde aos filtros.'}
              </p>
            ) : (
              <div className="clubes-leitor-grid">
                {clubesFiltrados.map((clube) => (
                  <button
                    type="button"
                    key={clube.id}
                    className={`clube-leitor-card ${clubeId === clube.id ? 'selecionado' : ''}`}
                    onClick={() => selecionarClube(clube.id)}
                  >
                    <span className="clube-leitor-titulo">{clube.nome}</span>
                    <span className="clube-leitor-descricao">{clube.descricao || 'Um espaço para compartilhar leituras.'}</span>
                    <span className="clube-leitor-meta">
                      {clube.categorias.map((categoria) => categoria.nome).join(' · ') || 'Sem categoria'}
                    </span>
                    <span className="clube-leitor-meta">
                      Até {clube.capacidadeMaxima} participantes
                      {clube.periodicidade ? ` · ${clube.periodicidade}` : ''}
                    </span>
                  </button>
                ))}
              </div>
            )}
          </section>
        )}

        {clubeId !== null && (
          <section className="avisos-card">
            <h2>Próximos encontros</h2>
            {carregandoEncontros && <p className="avisos-vazio">Carregando encontros...</p>}
            {!carregandoEncontros && encontrosVisiveis.length === 0 && (
              <p className="avisos-vazio">Este clube ainda não tem encontros abertos para inscrição.</p>
            )}
            <div className="clubes-encontros-leitor">
              {encontrosVisiveis.map((encontro) => {
                const inscricao = minhasInscricoes.get(encontro.id);
                const iniciado = agora === null || new Date(encontro.dataHora).getTime() <= agora;
                return (
                  <article key={encontro.id} className="encontro-leitor-card">
                    <div className="encontro-leitor-info">
                      <span className="encontro-livro">{encontro.livroTitulo}</span>
                      <span>{dataHoraMinBR(encontro.dataHora)}</span>
                      <span>{encontro.localOuAcesso}</span>
                      <span>
                        {encontro.vagasDisponiveis > 0
                          ? `${encontro.vagasDisponiveis} vaga(s) disponível(is)`
                          : 'Encontro lotado'}
                      </span>
                    </div>
                    {inscricao?.statusInscricao === 'cancelada' ? (
                      <span className="inscricao-cancelada">Inscrição cancelada</span>
                    ) : inscricao ? (
                      <div className="encontro-leitor-acao">
                        <span className="inscricao-confirmada">Inscrição confirmada</span>
                        <button
                          type="button"
                          className="avisos-btn sec"
                          disabled={iniciado || processandoId === encontro.id}
                          onClick={() => void cancelarInscricao(encontro)}
                        >
                          {processandoId === encontro.id ? 'Processando...' : 'Cancelar inscrição'}
                        </button>
                      </div>
                    ) : (
                      <button
                        type="button"
                        className="avisos-btn"
                        disabled={iniciado || encontro.vagasDisponiveis <= 0 || processandoId === encontro.id}
                        onClick={() => void inscrever(encontro)}
                      >
                        {processandoId === encontro.id ? 'Inscrevendo...' : 'Participar'}
                      </button>
                    )}
                  </article>
                );
              })}
            </div>
          </section>
        )}

        <section className="avisos-card">
          <div className="clube-inscricoes-header">
            <h2>Minhas inscrições</h2>
            <div className="clube-inscricoes-tabs">
              <button
                type="button"
                className={`avisos-btn ${abaInscricoes === 'proximos' ? '' : 'sec'}`}
                onClick={() => setAbaInscricoes('proximos')}
              >
                Próximos ({inscricoes.length})
              </button>
              <button
                type="button"
                className={`avisos-btn ${abaInscricoes === 'historico' ? '' : 'sec'}`}
                onClick={() => setAbaInscricoes('historico')}
              >
                Histórico ({historico.length})
              </button>
            </div>
          </div>
          {inscricoesVisiveis.length === 0 ? (
            <p className="avisos-vazio">
              {abaInscricoes === 'proximos' ? 'Você não tem inscrições futuras.' : 'Seu histórico de participações está vazio.'}
            </p>
          ) : (
            <div className="minhas-inscricoes-lista">
              {inscricoesVisiveis.map((inscricao) => (
                <article key={inscricao.id} className="minha-inscricao-item">
                  <div>
                    <strong>{inscricao.clubeNome} — {inscricao.livroTitulo}</strong>
                    <span>{dataHoraMinBR(inscricao.dataHora)} · {inscricao.localOuAcesso}</span>
                  </div>
                  <div className="minha-inscricao-status">
                    <span>{inscricao.statusInscricao === 'confirmada' ? 'Confirmada' : 'Cancelada'}</span>
                    {inscricao.presente !== null && (
                      <span>{inscricao.presente ? 'Presença registrada' : 'Ausência registrada'}</span>
                    )}
                    {inscricao.statusEncontro !== 'aberto' && <span>Encontro {inscricao.statusEncontro}</span>}
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>
      </main>
    </div>
  );
}
