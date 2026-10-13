import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Navbar } from '../components/Navbar';
import adicionarIcon from '../assets/adicionar.svg';
import './GerenciarLivros.css';

interface Livro {
  id: number;
  titulo: string;
  isbn: string;
  autor: string;
  categoria: string;
}

const LIVROS_MOCK: Livro[] = [
  {
    id: 1,
    titulo: 'Dom Casmurro',
    isbn: '978-85-359-0277-2',
    autor: 'Machado de Assis',
    categoria: 'Literatura Clássica Brasileira',
  },
  {
    id: 2,
    titulo: 'O Cortiço',
    isbn: '978-85-220-5517-0',
    autor: 'Aluísio Azevedo',
    categoria: 'Literatura Naturalista Brasileira',
  },
  {
    id: 3,
    titulo: 'Antologia Poética',
    isbn: '978-85-359-1258-9',
    autor: 'Carlos Drummond de Andrade',
    categoria: 'Poesia Brasileira',
  },
];

export function GerenciarLivros() {
  const navigate = useNavigate();
  const [livros, setLivros] = useState<Livro[]>(LIVROS_MOCK);

  const handleAdicionar = () => {
    navigate('/livros/cadastrar');
  };

  const handleEdit = (id: number) => {
    alert(`Editar livro ID #${id}`);
  };

  const handleDelete = (id: number) => {
    if (confirm('Deseja realmente excluir este livro?')) {
      setLivros(livros.filter((l) => l.id !== id));
    }
  };

  return (
    <div className="app-container">
      <Navbar userProfile={{ nome: 'Atendente' }} />

      <main className="gerenciar-livros-container">
        <div className="header-flex">
          <h1 className="page-title">Gerenciar Livros</h1>
          <button
            type="button"
            className="btn-add-livro"
            onClick={handleAdicionar}
            title="Cadastrar Novo Livro"
          >
            <img src={adicionarIcon} alt="Adicionar" />
          </button>
        </div>

        <div className="gerenciar-card">
          <div className="table-responsive">
            <table className="gerenciar-table">
              <thead>
                <tr>
                  <th className="col-titulo">Título</th>
                  <th>ISBN</th>
                  <th>Autor</th>
                  <th>Categoria</th>
                  <th className="col-acoes">Ações</th>
                </tr>
              </thead>
              <tbody>
                {livros.map((item) => (
                  <tr key={item.id}>
                    <td className="col-titulo">{item.titulo}</td>
                    <td>{item.isbn}</td>
                    <td>{item.autor}</td>
                    <td>{item.categoria}</td>
                    <td className="col-acoes">
                      <button
                        type="button"
                        className="btn-icon"
                        title="Editar livro"
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
                        title="Remover livro"
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