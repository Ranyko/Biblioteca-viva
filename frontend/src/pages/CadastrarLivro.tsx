import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Navbar } from '../components/Navbar';
import { getUsuario, textoErro } from '../services/api';
import {
  autoresService,
  categoriasService,
  livrosService,
  type Referencia,
} from '../services/catalogo';
import './CadastrarLivro.css';

interface ChecklistProps {
  rotulo: string;
  itens: Referencia[];
  selecionados: number[];
  onToggle: (id: number) => void;
  onCriar: (nome: string) => Promise<void>;
  placeholderNovo: string;
}

function Checklist({ rotulo, itens, selecionados, onToggle, onCriar, placeholderNovo }: ChecklistProps) {
  const [novo, setNovo] = useState('');

  const criar = async () => {
    const nome = novo.trim();
    if (!nome) return;
    await onCriar(nome);
    setNovo('');
  };

  return (
    <div className="form-group">
      <label>{rotulo}</label>
      <div style={{ maxHeight: 120, overflowY: 'auto', border: '1px solid #d1d5db', borderRadius: 8, padding: '0.5rem 0.75rem', background: '#fff' }}>
        {itens.length === 0 && <span style={{ color: '#6b7280' }}>Nenhum cadastrado.</span>}
        {itens.map((item) => (
          <label key={item.id} style={{ display: 'flex', gap: 8, alignItems: 'center', fontWeight: 400 }}>
            <input
              type="checkbox"
              checked={selecionados.includes(item.id)}
              onChange={() => onToggle(item.id)}
              style={{ width: 'auto' }}
            />
            {item.nome}
          </label>
        ))}
      </div>
      <div style={{ display: 'flex', gap: 8, marginTop: 6 }}>
        <input
          type="text"
          value={novo}
          placeholder={placeholderNovo}
          onChange={(e) => setNovo(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') {
              e.preventDefault();
              criar();
            }
          }}
        />
        <button type="button" className="btn-cancelar" onClick={criar}>
          Adicionar
        </button>
      </div>
    </div>
  );
}

export function CadastrarLivro() {
  const navigate = useNavigate();
  const { id } = useParams();
  const livroId = id ? Number(id) : null;
  const editando = livroId !== null;

  const [formData, setFormData] = useState({
    titulo: '',
    isbn: '',
    anoPublicacao: '',
    descricao: '',
    ativo: true,
  });
  const [autores, setAutores] = useState<Referencia[]>([]);
  const [categorias, setCategorias] = useState<Referencia[]>([]);
  const [autorIds, setAutorIds] = useState<number[]>([]);
  const [categoriaIds, setCategoriaIds] = useState<number[]>([]);
  const [erro, setErro] = useState('');
  const [salvando, setSalvando] = useState(false);

  useEffect(() => {
    let cancelado = false;
    (async () => {
      try {
        const [a, c] = await Promise.all([autoresService.listar(), categoriasService.listar()]);
        if (cancelado) return;
        setAutores(a);
        setCategorias(c);
        if (livroId !== null) {
          const livro = await livrosService.buscar(livroId);
          if (cancelado) return;
          setFormData({
            titulo: livro.titulo,
            isbn: livro.isbn,
            anoPublicacao: livro.anoPublicacao?.toString() ?? '',
            descricao: livro.descricao ?? '',
            ativo: livro.ativo,
          });
          setAutorIds(livro.autores.map((x) => x.id));
          setCategoriaIds(livro.categorias.map((x) => x.id));
        }
      } catch (err) {
        if (!cancelado) setErro(textoErro(err));
      }
    })();
    return () => {
      cancelado = true;
    };
  }, [livroId]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const toggle = (lista: number[], setLista: (v: number[]) => void, valor: number) =>
    setLista(lista.includes(valor) ? lista.filter((x) => x !== valor) : [...lista, valor]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErro('');
    setSalvando(true);
    try {
      const corpo = {
        titulo: formData.titulo.trim(),
        isbn: formData.isbn.trim(),
        anoPublicacao: formData.anoPublicacao ? Number(formData.anoPublicacao) : null,
        descricao: formData.descricao.trim() || null,
        ativo: formData.ativo,
        autorIds,
        categoriaIds,
      };
      if (livroId !== null) await livrosService.atualizar(livroId, corpo);
      else await livrosService.criar(corpo);
      navigate('/livros');
    } catch (err) {
      setErro(textoErro(err));
    } finally {
      setSalvando(false);
    }
  };

  const handleCancelar = () => {
    navigate('/livros');
  };

  return (
    <div className="app-container">
      <Navbar userProfile={{ nome: getUsuario()?.nome ?? 'Usuário' }} />

      <main className="cadastrar-livro-container">
        <h1 className="page-title">{editando ? 'Editar Livro' : 'Cadastrar Livro'}</h1>

        <div className="cadastrar-livro-card">
          <form onSubmit={handleSubmit} className="cadastrar-livro-form">
            {/* Coluna da Capa (o backend ainda não armazena capa) */}
            <div className="capa-column">
              <div className="capa-placeholder">
                <span className="placeholder-text">Capa do Livro</span>
              </div>
            </div>

            <div className="fields-column">
              {erro && <p style={{ color: '#b91c1c', margin: 0 }}>{erro}</p>}

              <div className="form-group">
                <label htmlFor="titulo">Título</label>
                <div className="input-with-icon">
                  <input
                    type="text"
                    id="titulo"
                    name="titulo"
                    placeholder="Insira o Título"
                    value={formData.titulo}
                    onChange={handleChange}
                    maxLength={200}
                    required
                  />
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="isbn">ISBN</label>
                <div className="input-with-icon">
                  <input
                    type="text"
                    id="isbn"
                    name="isbn"
                    placeholder="10 ou 13 caracteres (hífens são aceitos)"
                    value={formData.isbn}
                    onChange={handleChange}
                    maxLength={30}
                    required
                  />
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="anoPublicacao">Ano de publicação</label>
                <div className="input-with-icon">
                  <input
                    type="number"
                    id="anoPublicacao"
                    name="anoPublicacao"
                    placeholder="Opcional"
                    min={1000}
                    max={32767}
                    value={formData.anoPublicacao}
                    onChange={handleChange}
                  />
                </div>
              </div>

              <Checklist
                rotulo="Autores"
                itens={autores}
                selecionados={autorIds}
                onToggle={(x) => toggle(autorIds, setAutorIds, x)}
                placeholderNovo="Novo autor"
                onCriar={async (nome) => {
                  try {
                    const novo = await autoresService.criar(nome);
                    setAutores((l) => [...l, novo]);
                    setAutorIds((l) => [...l, novo.id]);
                  } catch (err) {
                    setErro(textoErro(err));
                  }
                }}
              />

              <Checklist
                rotulo="Categorias"
                itens={categorias}
                selecionados={categoriaIds}
                onToggle={(x) => toggle(categoriaIds, setCategoriaIds, x)}
                placeholderNovo="Nova categoria"
                onCriar={async (nome) => {
                  try {
                    const novo = await categoriasService.criar(nome);
                    setCategorias((l) => [...l, novo]);
                    setCategoriaIds((l) => [...l, novo.id]);
                  } catch (err) {
                    setErro(textoErro(err));
                  }
                }}
              />

              <div className="form-group">
                <label htmlFor="descricao">Descrição</label>
                <div className="input-with-icon">
                  <textarea
                    id="descricao"
                    name="descricao"
                    placeholder="Opcional"
                    rows={3}
                    value={formData.descricao}
                    onChange={handleChange}
                    style={{ width: '100%', boxSizing: 'border-box' }}
                  />
                </div>
              </div>

              <div className="form-actions">
                <button type="button" className="btn-cancelar" onClick={handleCancelar}>
                  Cancelar
                </button>
                <button type="submit" className="btn-cadastrar" disabled={salvando}>
                  {salvando ? 'Salvando...' : editando ? 'Salvar' : 'Cadastrar'}
                </button>
              </div>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}