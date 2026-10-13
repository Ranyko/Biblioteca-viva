import { api } from './api';

export interface Referencia { id: number; nome: string }

export interface Pagina<T> { itens: T[]; pagina: number; tamanho: number; total: number }

export interface Livro {
  id: number;
  titulo: string;
  isbn: string;
  anoPublicacao: number | null;
  descricao: string | null;
  ativo: boolean;
  autores: Referencia[];
  categorias: Referencia[];
}

export interface LivroRequest {
  titulo: string;
  isbn: string;
  anoPublicacao?: number | null;
  descricao?: string | null;
  ativo: boolean;
  autorIds: number[];
  categoriaIds: number[];
}

export type EstadoConservacao = 'novo' | 'bom' | 'regular' | 'danificado';
export type StatusExemplar = 'disponivel' | 'emprestado' | 'reservado' | 'inativo';

export interface Exemplar {
  id: number;
  livroId: number;
  codigoTombo: string;
  estadoConservacao: EstadoConservacao;
  status: StatusExemplar;
  cadastradoEm: string;
}

export const autoresService = {
  listar: () => api<Referencia[]>('/admin/autores?tamanho=100'),
  criar: (nome: string) => api<Referencia>('/admin/autores', { method: 'POST', body: { nome } }),
};

export const categoriasService = {
  listar: () => api<Referencia[]>('/admin/categorias?tamanho=100'),
  criar: (nome: string) => api<Referencia>('/admin/categorias', { method: 'POST', body: { nome } }),
};

export const livrosService = {
  listar: (pagina = 0, tamanho = 100) =>
    api<Pagina<Livro>>(`/admin/livros?pagina=${pagina}&tamanho=${tamanho}`),
  buscar: (id: number) => api<Livro>(`/admin/livros/${id}`),
  criar: (dados: LivroRequest) => api<Livro>('/admin/livros', { method: 'POST', body: dados }),
  // PUT é cadastro completo: sempre enviar todos os campos (senão os vínculos são apagados).
  atualizar: (id: number, dados: LivroRequest) =>
    api<Livro>(`/admin/livros/${id}`, { method: 'PUT', body: dados }),
  /** "Excluir" = inativar. O backend não tem DELETE; reenvia o livro completo com ativo=false. */
  definirAtivo: (livro: Livro, ativo: boolean) =>
    api<Livro>(`/admin/livros/${livro.id}`, {
      method: 'PUT',
      body: {
        titulo: livro.titulo,
        isbn: livro.isbn,
        anoPublicacao: livro.anoPublicacao,
        descricao: livro.descricao,
        ativo,
        autorIds: livro.autores.map((a) => a.id),
        categoriaIds: livro.categorias.map((c) => c.id),
      } satisfies LivroRequest,
    }),
};

export const exemplaresService = {
  listar: (pagina = 0, tamanho = 100, livroId?: number) =>
    api<Pagina<Exemplar>>(
      `/admin/exemplares?pagina=${pagina}&tamanho=${tamanho}${livroId ? `&livroId=${livroId}` : ''}`,
    ),
  criar: (dados: { livroId: number; codigoTombo: string; estadoConservacao: EstadoConservacao }) =>
    api<Exemplar>('/admin/exemplares', { method: 'POST', body: dados }),
  atualizar: (id: number, dados: { codigoTombo: string; estadoConservacao: EstadoConservacao }) =>
    api<Exemplar>(`/admin/exemplares/${id}`, { method: 'PUT', body: dados }),
  definirAtivo: (id: number, ativo: boolean) =>
    api<Exemplar>(`/admin/exemplares/${id}/situacao`, { method: 'PATCH', body: { ativo } }),
};
