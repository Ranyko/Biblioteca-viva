const API_URL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

export interface ApiErrorBody {
  status?: number;
  erro?: string;
  mensagem?: string;
  campos?: Record<string, string>;
}

export class ApiError extends Error {
  status: number;
  campos: Record<string, string>;

  constructor(status: number, mensagem: string, campos: Record<string, string> = {}) {
    super(mensagem);
    this.status = status;
    this.campos = campos;
  }
}

export function getToken(): string | null {
  return localStorage.getItem('token');
}

export function limparSessao() {
  localStorage.removeItem('token');
  localStorage.removeItem('usuario');
}

export function getUsuario(): { id: number; nome: string; email: string; perfil: string } | null {
  try {
    const raw = localStorage.getItem('usuario');
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

function mensagemPadrao(status: number): string {
  switch (status) {
    case 400: return 'Dados inválidos. Revise os campos.';
    case 401: return 'Sessão expirada. Faça login novamente.';
    case 403: return 'Você não tem permissão para esta ação.';
    case 404: return 'Registro não encontrado.';
    case 409: return 'Conflito: já existe um registro com esses dados.';
    default: return 'Erro inesperado no servidor.';
  }
}

export async function api<T>(
  path: string,
  options: { method?: string; body?: unknown } = {},
): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = {};
  if (options.body !== undefined) headers['Content-Type'] = 'application/json';
  if (token) headers['Authorization'] = `Bearer ${token}`;

  let response: Response;
  try {
    response = await fetch(`${API_URL}${path}`, {
      method: options.method ?? 'GET',
      headers,
      body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
    });
  } catch {
    throw new ApiError(0, 'Não foi possível conectar ao servidor.');
  }

  if (response.status === 401) {
    // Token ausente/expirado: limpa a sessão e volta para o login.
    limparSessao();
    if (window.location.pathname !== '/') window.location.assign('/');
    throw new ApiError(401, mensagemPadrao(401));
  }

  if (!response.ok) {
    let body: ApiErrorBody = {};
    try {
      body = await response.json();
    } catch {
      /* 403 do filtro de segurança vem sem corpo */
    }
    throw new ApiError(
      response.status,
      body.mensagem || mensagemPadrao(response.status),
      body.campos ?? {},
    );
  }

  if (response.status === 204) return undefined as T;
  const text = await response.text();
  return (text ? JSON.parse(text) : undefined) as T;
}

/** Texto único para exibir em formulários, incluindo erros por campo (400). */
export function textoErro(err: unknown): string {
  if (err instanceof ApiError) {
    const campos = Object.entries(err.campos).map(([c, m]) => `${c}: ${m}`);
    return campos.length ? `${err.message} (${campos.join('; ')})` : err.message;
  }
  return 'Erro inesperado.';
}
