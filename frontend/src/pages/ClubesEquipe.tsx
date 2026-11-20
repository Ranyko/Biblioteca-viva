import { useCallback, useEffect, useMemo, useState } from 'react';
import { Navbar } from '../components/Navbar';
import { getUsuario, textoErro } from '../services/api';
import { categoriasService, type Referencia } from '../services/catalogo';
import { listarAcervoCompleto, type ObraAcervo } from '../services/acervo';
import {
  clubesLeituraService,
  type ClubeLeitura,
  type ClubeLeituraRequest,
  type EncontroClube,
  type ParticipanteEncontro,
} from '../services/clubesLeitura';
import { dataHoraMinBR } from '../services/formato';
import './Avisos.css';
import './ClubesLeitura.css';
import { Mensagem } from '../components/Mensagem';

const novoClube: ClubeLeituraRequest = {
  nome: '',
  descricao: '',
  capacidadeMaxima: 12,
  periodicidade: '',
  categoriaIds: [],
};

function dataLocalInput(iso: string): string {
  const data = new Date(iso);
  const dois = (valor: number) => String(valor).padStart(2, '0');
  return `${dois(data.getDate())}/${dois(data.getMonth() + 1)}/${data.getFullYear()} - ${dois(data.getHours())}:${dois(data.getMinutes())}`;
}

function formatarDataHoraDigitada(valor: string): string {
  const digitos = valor.replace(/\D/g, '').slice(0, 12);
  const dia = digitos.slice(0, 2);
  const mes = digitos.slice(2, 4);
  const ano = digitos.slice(4, 8);
  const hora = digitos.slice(8, 10);
  const minuto = digitos.slice(10, 12);

  let resultado = dia;
  if (digitos.length > 2) resultado += `/${mes}`;
  if (digitos.length > 4) resultado += `/${ano}`;
  if (digitos.length > 8) resultado += ` - ${hora}`;
  if (digitos.length > 10) resultado += `:${minuto}`;
  return resultado;
}

function converterDataHoraLocal(valor: string): string | null {
  const partes = /^(\d{2})\/(\d{2})\/(\d{4}) - (\d{2}):(\d{2})$/.exec(valor);
  if (!partes) return null;

  const [, diaTexto, mesTexto, anoTexto, horaTexto, minutoTexto] = partes;
  const dia = Number(diaTexto);
  const mes = Number(mesTexto);
  const ano = Number(anoTexto);
  const hora = Number(horaTexto);
  const minuto = Number(minutoTexto);
  const data = new Date(ano, mes - 1, dia, hora, minuto);

  if (
    data.getFullYear() !== ano
    || data.getMonth() !== mes - 1
    || data.getDate() !== dia
    || data.getHours() !== hora
    || data.getMinutes() !== minuto
  ) return null;

  return data.toISOString();
}

export function ClubesEquipe() {
  const usuario = getUsuario();
  const administrador = usuario?.perfil === 'administrador';
  const [clubes, setClubes] = useState<ClubeLeitura[]>([]);
  const [categorias, setCategorias] = useState<Referencia[]>([]);
  const [livros, setLivros] = useState<ObraAcervo[]>([]);
  const [clubeId, setClubeId] = useState<number | null>(null);
  const [encontros, setEncontros] = useState<EncontroClube[]>([]);
  const [participantes, setParticipantes] = useState<Record<number, ParticipanteEncontro[]>>({});
  const [carregandoParticipantesId, setCarregandoParticipantesId] = useState<number | null>(null);
  const [errosParticipantes, setErrosParticipantes] = useState<Record<number, string>>({});
  const [abertoParticipantesId, setAbertoParticipantesId] = useState<number | null>(null);
  const [carregando, setCarregando] = useState(true);
  const [agora, setAgora] = useState<number | null>(null);
  const [erro, setErro] = useState('');
  const [mensagem, setMensagem] = useState('');
  const [salvando, setSalvando] = useState(false);
  const [clubeForm, setClubeForm] = useState<ClubeLeituraRequest>(novoClube);
  const [clubeEditandoId, setClubeEditandoId] = useState<number | null>(null);
  const [encontroEditandoId, setEncontroEditandoId] = useState<number | null>(null);
  const [encontroForm, setEncontroForm] = useState({ livroId: '', dataHora: '', localOuAcesso: '' });

  const carregarClubes = useCallback(async () => {
    const lista = await clubesLeituraService.listarClubesEquipe(administrador);
    setClubes(lista);
    setClubeId((atual) => atual && lista.some((clube) => clube.id === atual)
      ? atual
      : lista[0]?.id ?? null,
    );
  }, [administrador]);

  useEffect(() => {
    let cancelado = false;
    (async () => {
      try {
        const [listaClubes, listaCategorias, acervo] = await Promise.all([
          clubesLeituraService.listarClubesEquipe(administrador),
          administrador ? categoriasService.listar() : Promise.resolve([]),
          listarAcervoCompleto(),
        ]);
        if (cancelado) return;
        setClubes(listaClubes);
        setClubeId(listaClubes[0]?.id ?? null);
        setCategorias(listaCategorias);
        setLivros(acervo);
      } catch (err) {
        if (!cancelado) setErro(textoErro(err));
      } finally {
        if (!cancelado) setCarregando(false);
      }
    })();
    return () => {
      cancelado = true;
    };
  }, [administrador]);

  useEffect(() => {
    const timer = window.setTimeout(() => setAgora(Date.now()), 0);
    const intervalo = window.setInterval(() => setAgora(Date.now()), 60_000);
    return () => {
      window.clearTimeout(timer);
      window.clearInterval(intervalo);
    };
  }, []);

  useEffect(() => {
    if (clubeId === null) {
      return;
    }
    let cancelado = false;
    void (async () => {
      setParticipantes({});
      setAbertoParticipantesId(null);
      try {
        const lista = await clubesLeituraService.listarEncontrosEquipe(clubeId);
        if (!cancelado) setEncontros(lista);
      } catch (err) {
        if (!cancelado) setErro(textoErro(err));
      }
    })();
    return () => {
      cancelado = true;
    };
  }, [clubeId]);

  const clubeSelecionado = useMemo(
    () => clubes.find((clube) => clube.id === clubeId) ?? null,
    [clubes, clubeId],
  );

  const selecionarClube = (id: number) => {
    if (id !== clubeId) {
      setEncontros([]);
      setParticipantes({});
      setAbertoParticipantesId(null);
      setErro('');
    }
    setClubeId(id);
  };

  const editarClube = (clube: ClubeLeitura) => {
    setClubeEditandoId(clube.id);
    setClubeForm({
      nome: clube.nome,
      descricao: clube.descricao ?? '',
      capacidadeMaxima: clube.capacidadeMaxima,
      periodicidade: clube.periodicidade ?? '',
      categoriaIds: clube.categorias.map((categoria) => categoria.id),
    });
    setErro('');
    setMensagem('');
  };

  const salvarClube = async (event: React.FormEvent) => {
    event.preventDefault();
    setErro('');
    setMensagem('');
    setSalvando(true);
    try {
      const resultado = clubeEditandoId
        ? await clubesLeituraService.atualizarClube(clubeEditandoId, clubeForm)
        : await clubesLeituraService.criarClube(clubeForm);
      await carregarClubes();
      setClubeId(resultado.id);
      setClubeForm(novoClube);
      setClubeEditandoId(null);
      setMensagem(clubeEditandoId ? 'Clube atualizado.' : 'Clube criado.');
    } catch (err) {
      setErro(textoErro(err));
    } finally {
      setSalvando(false);
    }
  };

  const mudarStatusClube = async (clube: ClubeLeitura) => {
    setErro('');
    setMensagem('');
    setSalvando(true);
    try {
      await clubesLeituraService.alterarStatusClube(
        clube.id,
        clube.status === 'ativo' ? 'encerrado' : 'ativo',
      );
      await carregarClubes();
      setMensagem(clube.status === 'ativo' ? 'Clube encerrado.' : 'Clube reativado.');
    } catch (err) {
      setErro(textoErro(err));
    } finally {
      setSalvando(false);
    }
  };

  const limparEncontro = () => {
    setEncontroEditandoId(null);
    setEncontroForm({ livroId: '', dataHora: '', localOuAcesso: '' });
  };

  const salvarEncontro = async (event: React.FormEvent) => {
    event.preventDefault();
    if (clubeId === null) return;
    setErro('');
    setMensagem('');
    setSalvando(true);
    try {
      const dataHora = converterDataHoraLocal(encontroForm.dataHora);
      if (!dataHora) {
        setErro('Informe uma data e hora válida no formato dd/MM/yyyy - HH:mm.');
        return;
      }
      if (new Date(dataHora).getTime() <= Date.now()) {
        setErro('A data e hora do encontro devem estar no futuro.');
        return;
      }
      const dados = {
        livroId: Number(encontroForm.livroId),
        dataHora,
        localOuAcesso: encontroForm.localOuAcesso.trim(),
      };
      const editando = encontroEditandoId !== null;
      if (encontroEditandoId) {
        await clubesLeituraService.atualizarEncontro(clubeId, encontroEditandoId, dados);
      } else {
        await clubesLeituraService.criarEncontro(clubeId, dados);
      }
      limparEncontro();
      setMensagem(editando ? 'Encontro atualizado.' : 'Encontro agendado.');
      try {
        setEncontros(await clubesLeituraService.listarEncontrosEquipe(clubeId));
      } catch (err) {
        setErro(`Encontro ${editando ? 'atualizado' : 'agendado'}, mas não foi possível atualizar a lista: ${textoErro(err)}`);
      }
    } catch (err) {
      setErro(textoErro(err));
    } finally {
      setSalvando(false);
    }
  };

  const editarEncontro = (encontro: EncontroClube) => {
    setEncontroEditandoId(encontro.id);
    setEncontroForm({
      livroId: String(encontro.livroId),
      dataHora: dataLocalInput(encontro.dataHora),
      localOuAcesso: encontro.localOuAcesso,
    });
    setErro('');
    setMensagem('');
  };

  const cancelarEncontro = async (encontro: EncontroClube) => {
    if (clubeId === null) return;
    setErro('');
    setMensagem('');
    setSalvando(true);
    try {
      await clubesLeituraService.cancelarEncontro(clubeId, encontro.id);
      setEncontros(await clubesLeituraService.listarEncontrosEquipe(clubeId));
      setMensagem('Encontro cancelado.');
    } catch (err) {
      setErro(textoErro(err));
    } finally {
      setSalvando(false);
    }
  };

  const buscarParticipantes = async (encontro: EncontroClube) => {
    setCarregandoParticipantesId(encontro.id);
    setErrosParticipantes((atuais) => ({ ...atuais, [encontro.id]: '' }));
    try {
      const lista = await clubesLeituraService.listarParticipantes(encontro.clubeId, encontro.id);
      setParticipantes((atuais) => ({ ...atuais, [encontro.id]: lista }));
    } catch (err) {
      setErrosParticipantes((atuais) => ({ ...atuais, [encontro.id]: textoErro(err) }));
    } finally {
      setCarregandoParticipantesId(null);
    }
  };

  const carregarParticipantes = async (encontro: EncontroClube) => {
    if (abertoParticipantesId === encontro.id) {
      setAbertoParticipantesId(null);
      return;
    }
    setAbertoParticipantesId(encontro.id);
    if (participantes[encontro.id] || carregandoParticipantesId === encontro.id) return;
    await buscarParticipantes(encontro);
  };

  const registrarPresenca = async (encontro: EncontroClube, leitorId: number, presente: boolean) => {
    setErro('');
    setMensagem('');
    try {
      const participante = await clubesLeituraService.registrarPresenca(
        encontro.clubeId,
        encontro.id,
        leitorId,
        presente,
      );
      setParticipantes((atuais) => ({
        ...atuais,
        [encontro.id]: (atuais[encontro.id] ?? []).map((item) =>
          item.leitorId === leitorId ? participante : item,
        ),
      }));
      setMensagem(`Presença de ${participante.nome} atualizada.`);
    } catch (err) {
      setErro(textoErro(err));
    }
  };

  const concluirEncontro = async (encontro: EncontroClube) => {
    setErro('');
    setMensagem('');
    setSalvando(true);
    try {
      const atualizado = await clubesLeituraService.concluirEncontro(encontro.clubeId, encontro.id);
      setEncontros((atuais) => atuais.map((item) => item.id === atualizado.id ? atualizado : item));
      setMensagem('Encontro concluído.');
    } catch (err) {
      setErro(textoErro(err));
    } finally {
      setSalvando(false);
    }
  };

  const inicio = (encontro: EncontroClube) =>
    agora !== null && new Date(encontro.dataHora).getTime() <= agora;

  return (
    <div className="avisos-page">
      <Navbar userProfile={{ nome: usuario?.nome ?? 'Equipe' }} />
      <main className="avisos-main clubes-main">
        <div className="avisos-header">
          <div>
            <h1 className="avisos-titulo">Clubes de leitura</h1>
            <p className="clubes-subtitulo">Organize clubes e encontros e acompanhe as participações.</p>
          </div>
        </div>
        {erro && <Mensagem tipo="erro">{erro}</Mensagem>}
        {mensagem && <Mensagem tipo="sucesso">{mensagem}</Mensagem>}
        {carregando && <p className="avisos-vazio">Carregando clubes...</p>}

        {administrador && (
          <section className="avisos-card">
            <h2>{clubeEditandoId ? 'Editar clube' : 'Criar clube'}</h2>
            <form className="clube-form" onSubmit={(event) => void salvarClube(event)}>
              <label>
                Nome
                <input
                  required
                  maxLength={160}
                  value={clubeForm.nome}
                  onChange={(event) => setClubeForm((atual) => ({ ...atual, nome: event.target.value }))}
                />
              </label>
              <label>
                Descrição
                <textarea
                  rows={3}
                  value={clubeForm.descricao}
                  onChange={(event) => setClubeForm((atual) => ({ ...atual, descricao: event.target.value }))}
                />
              </label>
              <div className="clube-form-grid">
                <label>
                  Capacidade máxima
                  <input
                    type="number"
                    min={1}
                    max={32767}
                    required
                    value={clubeForm.capacidadeMaxima}
                    onChange={(event) => setClubeForm((atual) => ({
                      ...atual,
                      capacidadeMaxima: Number(event.target.value),
                    }))}
                  />
                </label>
                <label>
                  Periodicidade
                  <input
                    maxLength={80}
                    value={clubeForm.periodicidade}
                    onChange={(event) => setClubeForm((atual) => ({ ...atual, periodicidade: event.target.value }))}
                    placeholder="Ex.: Mensal"
                  />
                </label>
              </div>
              {categorias.length > 0 && (
                <fieldset className="clube-categorias">
                  <legend>Categorias de interesse</legend>
                  <div className="clube-categorias-lista">
                    {categorias.map((categoria) => (
                      <label key={categoria.id}>
                        <input
                          type="checkbox"
                          checked={clubeForm.categoriaIds.includes(categoria.id)}
                          onChange={(event) => setClubeForm((atual) => ({
                            ...atual,
                            categoriaIds: event.target.checked
                              ? [...atual.categoriaIds, categoria.id]
                              : atual.categoriaIds.filter((id) => id !== categoria.id),
                          }))}
                        />
                        {categoria.nome}
                      </label>
                    ))}
                  </div>
                </fieldset>
              )}
              <div className="clube-form-acoes">
                {clubeEditandoId && (
                  <button type="button" className="avisos-btn sec" onClick={() => {
                    setClubeEditandoId(null);
                    setClubeForm(novoClube);
                  }}>
                    Cancelar edição
                  </button>
                )}
                <button type="submit" className="avisos-btn" disabled={salvando}>
                  {salvando ? 'Salvando...' : clubeEditandoId ? 'Salvar alterações' : 'Criar clube'}
                </button>
              </div>
            </form>
          </section>
        )}

        {!carregando && clubes.length === 0 && (
          <section className="avisos-card"><p className="avisos-vazio">Nenhum clube cadastrado.</p></section>
        )}
        {clubes.length > 0 && (
          <section className="avisos-card">
            <h2>Clubes</h2>
            <div className="clubes-lista-equipe">
              {clubes.map((clube) => (
                <article key={clube.id} className={`clube-equipe-item ${clubeId === clube.id ? 'selecionado' : ''}`}>
                  <button type="button" className="clube-selecionar" onClick={() => selecionarClube(clube.id)}>
                    <span>
                      <strong>{clube.nome}</strong>
                      <small>{clube.categorias.map((categoria) => categoria.nome).join(' · ') || 'Sem categoria'}</small>
                    </span>
                    <span className={`clube-status ${clube.status}`}>{clube.status === 'ativo' ? 'Ativo' : 'Encerrado'}</span>
                  </button>
                  {administrador && (
                    <div className="clube-equipe-acoes">
                      <button type="button" className="avisos-btn sec" onClick={() => editarClube(clube)}>Editar</button>
                      <button
                        type="button"
                        className="avisos-btn sec"
                        disabled={salvando}
                        onClick={() => void mudarStatusClube(clube)}
                      >
                        {clube.status === 'ativo' ? 'Encerrar' : 'Reativar'}
                      </button>
                    </div>
                  )}
                </article>
              ))}
            </div>
          </section>
        )}

        {clubeSelecionado && (
          <section className="avisos-card">
            <div className="clube-encontros-titulo">
              <div>
                <h2>Encontros — {clubeSelecionado.nome}</h2>
                <p className="avisos-vazio">
                  Capacidade: {clubeSelecionado.capacidadeMaxima} participantes
                  {clubeSelecionado.periodicidade ? ` · ${clubeSelecionado.periodicidade}` : ''}
                </p>
              </div>
            </div>

            {clubeSelecionado.status === 'ativo' && (
              <form className="clube-form encontro-form" onSubmit={(event) => void salvarEncontro(event)}>
                <h3>{encontroEditandoId ? 'Editar encontro' : 'Agendar encontro'}</h3>
                <div className="clube-form-grid">
                  <label>
                    Obra do acervo
                    <select
                      required
                      value={encontroForm.livroId}
                      onChange={(event) => setEncontroForm((atual) => ({ ...atual, livroId: event.target.value }))}
                    >
                      <option value="">Selecione um livro</option>
                      {livros.map((livro) => <option key={livro.id} value={livro.id}>{livro.titulo}</option>)}
                    </select>
                  </label>
                  <label>
                    Data e hora
                    <input
                      type="text"
                      required
                      inputMode="numeric"
                      maxLength={18}
                      placeholder="dd/MM/yyyy - HH:mm"
                      pattern="\d{2}/\d{2}/\d{4} - \d{2}:\d{2}"
                      value={encontroForm.dataHora}
                      onChange={(event) => setEncontroForm((atual) => ({
                        ...atual,
                        dataHora: formatarDataHoraDigitada(event.target.value),
                      }))}
                    />
                  </label>
                </div>
                <label>
                  Local ou instruções de acesso
                  <textarea
                    required
                    maxLength={1000}
                    rows={2}
                    value={encontroForm.localOuAcesso}
                    onChange={(event) => setEncontroForm((atual) => ({ ...atual, localOuAcesso: event.target.value }))}
                  />
                </label>
                <div className="clube-form-acoes">
                  {encontroEditandoId && (
                    <button type="button" className="avisos-btn sec" onClick={limparEncontro}>Cancelar edição</button>
                  )}
                  <button type="submit" className="avisos-btn" disabled={salvando || livros.length === 0}>
                    {salvando ? 'Salvando...' : encontroEditandoId ? 'Salvar encontro' : 'Agendar encontro'}
                  </button>
                </div>
                {livros.length === 0 && <p className="avisos-vazio">Não há livros disponíveis no acervo para associar.</p>}
              </form>
            )}

            {encontros.length === 0 ? (
              <p className="avisos-vazio">Este clube ainda não tem encontros.</p>
            ) : (
              <div className="clube-encontros-lista">
                {encontros.map((encontro) => (
                  <article key={encontro.id} className="encontro-equipe-item">
                    <div className="encontro-equipe-resumo">
                      <div>
                        <h3>{encontro.livroTitulo}</h3>
                        <p>{dataHoraMinBR(encontro.dataHora)} · {encontro.localOuAcesso}</p>
                        <p>
                          {encontro.inscricoesConfirmadas}/{encontro.capacidadeMaxima} inscritos
                          {encontro.vagasDisponiveis > 0 ? ` · ${encontro.vagasDisponiveis} vagas` : ' · Lotado'}
                        </p>
                      </div>
                      <span className={`clube-status ${encontro.status}`}>{encontro.status}</span>
                    </div>
                    <div className="clube-form-acoes">
                      <button type="button" className="avisos-btn sec" onClick={() => void carregarParticipantes(encontro)}>
                        {abertoParticipantesId === encontro.id ? 'Ocultar participantes' : 'Participantes'}
                      </button>
                      {encontro.status === 'aberto' && agora !== null && !inicio(encontro) && (
                        <>
                          <button type="button" className="avisos-btn sec" onClick={() => editarEncontro(encontro)}>Editar</button>
                          <button type="button" className="avisos-btn sec" disabled={salvando} onClick={() => void cancelarEncontro(encontro)}>
                            Cancelar encontro
                          </button>
                        </>
                      )}
                      {encontro.status === 'aberto' && inicio(encontro) && (
                        <button type="button" className="avisos-btn" disabled={salvando} onClick={() => void concluirEncontro(encontro)}>
                          Concluir encontro
                        </button>
                      )}
                    </div>
                    {abertoParticipantesId === encontro.id && (
                      <div className="participantes-lista">
                        {carregandoParticipantesId === encontro.id && (
                          <p className="avisos-vazio">Carregando participantes...</p>
                        )}
                        {errosParticipantes[encontro.id] && (
                          <div>
                            <Mensagem tipo="erro">{errosParticipantes[encontro.id]}</Mensagem>
                            <button
                              type="button"
                              className="avisos-btn sec"
                              disabled={carregandoParticipantesId === encontro.id}
                              onClick={() => void buscarParticipantes(encontro)}
                            >
                              Tentar novamente
                            </button>
                          </div>
                        )}
                        {!carregandoParticipantesId
                          && !errosParticipantes[encontro.id]
                          && participantes[encontro.id]?.length === 0
                          && <p className="avisos-vazio">Nenhuma inscrição neste encontro.</p>}
                        {!carregandoParticipantesId
                          && !errosParticipantes[encontro.id]
                          && participantes[encontro.id]?.map((participante) => (
                          <div key={participante.leitorId} className="participante-item">
                            <span>
                              <strong>{participante.nome}</strong>
                              <small>
                                {participante.statusInscricao === 'cancelada' ? 'Inscrição cancelada' : 'Inscrição confirmada'}
                                {participante.presente !== null ? ` · ${participante.presente ? 'Presente' : 'Ausente'}` : ''}
                              </small>
                            </span>
                            {encontro.status === 'aberto' && inicio(encontro) && participante.statusInscricao === 'confirmada' && (
                              <span className="participante-acoes">
                                <button
                                  type="button"
                                  className="avisos-btn sec"
                                  disabled={participante.presente === true}
                                  onClick={() => void registrarPresenca(encontro, participante.leitorId, true)}
                                >
                                  Presente
                                </button>
                                <button
                                  type="button"
                                  className="avisos-btn sec"
                                  disabled={participante.presente === false}
                                  onClick={() => void registrarPresenca(encontro, participante.leitorId, false)}
                                >
                                  Ausente
                                </button>
                              </span>
                            )}
                          </div>
                        ))}
                      </div>
                    )}
                  </article>
                ))}
              </div>
            )}
          </section>
        )}
      </main>
    </div>
  );
}
