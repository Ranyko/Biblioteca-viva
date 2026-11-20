import type { CSSProperties, ReactNode } from 'react';
import './Mensagem.css';

export type TipoMensagem = 'erro' | 'sucesso' | 'aviso' | 'info';

const ICONES: Record<TipoMensagem, ReactNode> = {
  erro: (
    <>
      <circle cx="12" cy="12" r="10" />
      <line x1="12" y1="8" x2="12" y2="12" />
      <line x1="12" y1="16" x2="12.01" y2="16" />
    </>
  ),
  sucesso: (
    <>
      <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
      <polyline points="22 4 12 14.01 9 11.01" />
    </>
  ),
  aviso: (
    <>
      <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
      <line x1="12" y1="9" x2="12" y2="13" />
      <line x1="12" y1="17" x2="12.01" y2="17" />
    </>
  ),
  info: (
    <>
      <circle cx="12" cy="12" r="10" />
      <line x1="12" y1="16" x2="12" y2="12" />
      <line x1="12" y1="8" x2="12.01" y2="8" />
    </>
  ),
};

interface MensagemProps {
  tipo: TipoMensagem;
  children: ReactNode;
  style?: CSSProperties;
}

/** Faixa de feedback padrão da aplicação (erro, sucesso, aviso e informação). */
export function Mensagem({ tipo, children, style }: MensagemProps) {
  return (
    <div className={`msg msg-${tipo}`} role={tipo === 'erro' ? 'alert' : 'status'} style={style}>
      <svg
        className="msg-icone"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
        aria-hidden="true"
      >
        {ICONES[tipo]}
      </svg>
      <div className="msg-corpo">{children}</div>
    </div>
  );
}
