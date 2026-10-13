import { useState } from 'react';
import { Navbar } from '../components/Navbar';
import './GerenciarExemplares.css';

interface Exemplar {
  id: number;
  codigo: string;
  titulo: string;
  estadoConservacao: 'Novo' | 'Bom' | 'Regular' | 'Ruim';
  status: 'Disponível' | 'Emprestado' | 'Reservado';
}

const EXEMPLARES_MOCK: Exemplar[] = [
  {
    id: 1,
    codigo: '0001',
    titulo: 'Dom Casmurro',
    estadoConservacao: 'Bom',
    status: 'Disponível',
  },
  {
    id: 2,
    codigo: '0002',
    titulo: 'O Cortiço',
    estadoConservacao: 'Novo',
    status: 'Emprestado',
  },
  {
    id: 3,
    codigo: '0003',
    titulo: 'Antologia Poética',
    estadoConservacao: 'Regular',
    status: 'Reservado',
  },
];

export function GerenciarExemplares() {
  const [exemplares, setExemplares] = useState<Exemplar[]>(EXEMPLARES_MOCK);

  const handleEdit = (id: number) => {
    alert(`Editar exemplar ID #${id}`);
  };

  const handleDelete = (id: number) => {
    if (confirm('Deseja realmente remover este exemplar?')) {
      setExemplares(exemplares.filter((e) => e.id !== id));
    }
  };

  return (
    <div className="app-container">
      <Navbar userProfile={{ nome: 'Administrador' }} />

      <main className="gerenciar-exemplares-container">
        <div className="header-flex">
          <h1 className="page-title">Gerenciar Exemplares</h1>
        </div>

        <div className="gerenciar-card">
          <div className="table-responsive">
            <table className="gerenciar-table">
              <thead>
                <tr>
                  <th className="col-codigo">Código</th>
                  <th className="col-titulo">Título</th>
                  <th>Estado de conservação</th>
                  <th>Status</th>
                  <th className="col-acoes">Ações</th>
                </tr>
              </thead>
              <tbody>
                {exemplares.map((item) => (
                  <tr key={item.id}>
                    <td className="col-codigo">{item.codigo}</td>
                    <td className="col-titulo">{item.titulo}</td>
                    <td>{item.estadoConservacao}</td>
                    <td>{item.status}</td>
                    <td className="col-acoes">
                      <button
                        type="button"
                        className="btn-icon"
                        title="Editar exemplar"
                        onClick={() => handleEdit(item.id)}
                      >
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M12 20h9" />
                          <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" />
                        </svg>
                      </button>

                      <button
                        type="button"
                        className="btn-icon btn-delete"
                        title="Remover exemplar"
                        onClick={() => handleDelete(item.id)}
                      >
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <polyline points="3 6 5 6 21 6" />
                          <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                        </svg>
                      </button>
                    </td>
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