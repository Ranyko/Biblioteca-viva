import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Navbar } from '../components/Navbar';
import adicionarIcon from '../assets/adicionar.svg';
import { getUsuario, textoErro } from '../services/api';
import { leitoresService, type Leitor } from '../services/leitores';
import './GerenciarLeitores.css';

export function GerenciarLeitores() {
  const navigate = useNavigate();
  const [leitores, setLeitores] = useState<Leitor[]>([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');

  useEffect(() => {
    let cancelado = false;
    (async () => {
      try {
        const lista = await leitoresService.listar();
        if (!cancelado) setLeitores(lista);
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

  return (
    <div className="app-container">
      <Navbar userProfile={{ nome: getUsuario()?.nome ?? 'Usuário' }} />

      <main className="gerenciar-leitores-container">
        <div className="header-flex">
          <h1 className="page-title">Gerenciar Leitores</h1>
          <button
            type="button"
            className="btn-add-livro"
            onClick={() => navigate('/leitores/cadastrar')}
            title="Cadastrar Novo Leitor"
          >
            <img src={adicionarIcon} alt="Adicionar" />
          </button>
        </div>

        {erro && <p style={{ color: '#b91c1c', marginBottom: '1rem' }}>{erro}</p>}

        <div className="gerenciar-card">
          <div className="table-responsive">
            <table className="gerenciar-table">
              <thead>
                <tr>
                  <th className="col-titulo">Nome</th>
                  <th>E-mail</th>
                  <th>Documento</th>
                  <th>Telefone</th>
                  <th>Situação</th>
                </tr>
              </thead>
              <tbody>
                {carregando && (
                  <tr><td colSpan={5}>Carregando...</td></tr>
                )}
                {!carregando && leitores.length === 0 && !erro && (
                  <tr><td colSpan={5}>Nenhum leitor cadastrado.</td></tr>
                )}
                {leitores.map((l) => (
                  <tr key={l.id} style={l.ativo ? undefined : { opacity: 0.5 }}>
                    <td className="col-titulo">{l.nome}</td>
                    <td>{l.email}</td>
                    <td>{l.documento}</td>
                    <td>{l.telefone ?? '—'}</td>
                    <td>{l.ativo ? 'Ativo' : 'Inativo'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </div>
  );
}
