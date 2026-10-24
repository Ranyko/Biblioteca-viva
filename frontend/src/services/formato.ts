/** 'YYYY-MM-DD' (LocalDate do back) -> 'dd/mm/aaaa', sem passar por fuso horário. */
export function dataBR(dataIso: string): string {
  const [ano, mes, dia] = dataIso.slice(0, 10).split('-');
  return `${dia}/${mes}/${ano}`;
}

/** Data/hora com fuso (OffsetDateTime do back) -> 'dd/mm/aaaa' no horário de Brasília. */
export function dataHoraBR(iso: string): string {
  return new Date(iso).toLocaleDateString('pt-BR', { timeZone: 'America/Sao_Paulo' });
}

export function hojeBR(): string {
  return new Date().toLocaleDateString('pt-BR', { timeZone: 'America/Sao_Paulo' });
}

export function moeda(valor: number): string {
  return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

/** Dias corridos entre a data prevista (YYYY-MM-DD) e hoje; 0 se ainda não venceu. */
export function diasDeAtraso(dataPrevista: string): number {
  const [a, m, d] = dataPrevista.slice(0, 10).split('-').map(Number);
  const prevista = Date.UTC(a, m - 1, d);
  const [dd, mm, aa] = hojeBR().split('/').map(Number);
  const hoje = Date.UTC(aa, mm - 1, dd);
  return Math.max(0, Math.round((hoje - prevista) / 86_400_000));
}
