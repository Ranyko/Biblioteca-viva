import { api } from './api';
import type { Pagina } from './catalogo';

export interface ObraAcervo {
  id: number;
  titulo: string;
  isbn: string;
  anoPublicacao: number | null;
  descricao: string | null;
  autores: string[];
  categorias: string[];
  exemplaresDisponiveis: number;
}

const TAMANHO = 100; // máximo aceito pelo back

type Filtro = { titulo?: string; autor?: string; categoria?: string };

function buscar(filtro: Filtro) {
  const qs = new URLSearchParams({ pagina: '0', tamanho: String(TAMANHO) });
  for (const [campo, valor] of Object.entries(filtro)) if (valor) qs.set(campo, valor);
  return api<Pagina<ObraAcervo>>(`/acervo?${qs}`);
}

/**
 * Um único termo procura por título, autor ou categoria. O back filtra os três campos
 * em conjunto (E), então fazemos três consultas e juntamos os resultados (OU).
 * `truncado` indica que alguma consulta tinha mais de 100 obras (peça para refinar a busca).
 */
export async function pesquisarAcervo(termo: string): Promise<{ obras: ObraAcervo[]; truncado: boolean }> {
  const t = termo.trim();
  if (!t) {
    const r = await buscar({});
    return { obras: r.itens, truncado: r.total > r.itens.length };
  }
  const respostas = await Promise.all([buscar({ titulo: t }), buscar({ autor: t }), buscar({ categoria: t })]);
  const porId = new Map<number, ObraAcervo>();
  for (const r of respostas) for (const obra of r.itens) porId.set(obra.id, obra);
  const obras = [...porId.values()].sort((a, b) => a.titulo.localeCompare(b.titulo, 'pt-BR'));
  return { obras, truncado: respostas.some((r) => r.total > r.itens.length) };
}
