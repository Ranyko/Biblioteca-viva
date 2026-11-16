import { API_URL, ApiError, getToken, type ApiErrorBody } from './api';

/** H16 - baixa o CSV de movimentações (datas 'AAAA-MM-DD', ambas opcionais). */
export async function baixarMovimentacoesCsv(dataInicio?: string, dataFim?: string): Promise<void> {
  const qs = new URLSearchParams();
  if (dataInicio) qs.set('dataInicio', dataInicio);
  if (dataFim) qs.set('dataFim', dataFim);
  const sufixo = qs.toString() ? `?${qs}` : '';

  let resposta: Response;
  try {
    resposta = await fetch(`${API_URL}/admin/relatorios/movimentacoes/csv${sufixo}`, {
      headers: { Authorization: `Bearer ${getToken() ?? ''}` },
    });
  } catch {
    throw new ApiError(0, 'Não foi possível conectar ao servidor.');
  }

  if (!resposta.ok) {
    let corpo: ApiErrorBody = {};
    try {
      corpo = await resposta.json();
    } catch {
      /* 401/403 do filtro de segurança vêm sem corpo */
    }
    const padrao =
      resposta.status === 403
        ? 'Você não tem permissão para exportar o relatório.'
        : resposta.status === 401
          ? 'Não foi possível exportar (sessão expirada ou rota indisponível).'
          : 'Não foi possível gerar o relatório.';
    throw new ApiError(resposta.status, corpo.mensagem || padrao);
  }

  const url = URL.createObjectURL(await resposta.blob());
  const link = document.createElement('a');
  link.href = url;
  link.download = 'movimentacoes.csv';
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}
