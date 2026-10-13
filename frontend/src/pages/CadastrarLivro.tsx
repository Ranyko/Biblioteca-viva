import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Navbar } from '../components/Navbar';
import './CadastrarLivro.css';

export function CadastrarLivro() {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    titulo: '',
    isbn: '',
    autor: '',
    categoria: '',
    capaUrl: ''
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    console.log('Dados do livro para cadastro:', formData);
    alert('Formulário enviado com sucesso!');
  };

  const handleCancelar = () => {
    navigate('/acervo');
  };

  return (
    <div className="app-container">
      <Navbar userProfile={{ nome: 'Atendente' }} />

      <main className="cadastrar-livro-container">
        <h1 className="page-title">Cadastrar Livro</h1>

        <div className="cadastrar-livro-card">
          <form onSubmit={handleSubmit} className="cadastrar-livro-form">
            
            {/* Coluna da Capa */}
            <div className="capa-column">
              <div className="capa-placeholder">
                {formData.capaUrl ? (
                  <img src={formData.capaUrl} alt="Capa do livro" />
                ) : (
                  <span className="placeholder-text">Capa do Livro</span>
                )}
              </div>
            </div>

            {/* Coluna dos Campos do Formulário */}
            <div className="fields-column">
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
                  />
                  <svg className="search-icon-svg" viewBox="0 0 24 24" fill="none" stroke="#6B7280" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <circle cx="11" cy="11" r="8"></circle>
                  <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                </svg>
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="isbn">ISBN</label>
                <div className="input-with-icon">
                  <input
                    type="text"
                    id="isbn"
                    name="isbn"
                    placeholder="Insira o ISBN"
                    value={formData.isbn}
                    onChange={handleChange}
                  />
                  <svg className="search-icon-svg" viewBox="0 0 24 24" fill="none" stroke="#6B7280" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <circle cx="11" cy="11" r="8"></circle>
                  <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                </svg>
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="autor">Autor</label>
                <div className="input-with-icon">
                  <input
                    type="text"
                    id="autor"
                    name="autor"
                    placeholder="Digite o nome do autor"
                    value={formData.autor}
                    onChange={handleChange}
                  />
                  <svg className="search-icon-svg" viewBox="0 0 24 24" fill="none" stroke="#6B7280" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <circle cx="11" cy="11" r="8"></circle>
                  <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                </svg>
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="categoria">Categoria</label>
                <div className="input-with-icon">
                  <input
                    type="text"
                    id="categoria"
                    name="categoria"
                    placeholder="Selecione a categoria do livro"
                    value={formData.categoria}
                    onChange={handleChange}
                  />
                  <svg className="search-icon-svg" viewBox="0 0 24 24" fill="none" stroke="#6B7280" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                  <circle cx="11" cy="11" r="8"></circle>
                  <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                </svg>
                </div>
              </div>

              <div className="form-actions">
                <button
                  type="button"
                  className="btn-cancelar"
                  onClick={handleCancelar}
                >
                  Cancelar
                </button>
                <button type="submit" className="btn-cadastrar">
                  Cadastrar
                </button>
              </div>
            </div>

          </form>
        </div>
      </main>
    </div>
  );
}