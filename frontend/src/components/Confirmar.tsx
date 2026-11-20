import { useEffect, useRef } from 'react';
import './Confirmar.css';

export interface Pedido {
  texto: string;
  rotulo: string;
  perigo: boolean;
  resolver: (confirmou: boolean) => void;
}

export function Dialogo({ pedido, onResponder }: { pedido: Pedido; onResponder: (confirmou: boolean) => void }) {
  const botao = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    botao.current?.focus();
    const tecla = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onResponder(false);
    };
    document.addEventListener('keydown', tecla);
    return () => document.removeEventListener('keydown', tecla);
  }, [onResponder]);

  return (
    <div className="confirmar-fundo" onMouseDown={(e) => e.target === e.currentTarget && onResponder(false)}>
      <div className="confirmar-caixa" role="alertdialog" aria-modal="true" aria-labelledby="confirmar-titulo">
        <h2 id="confirmar-titulo">Confirmar ação</h2>
        <p>{pedido.texto}</p>
        <div className="confirmar-acoes">
          <button type="button" className="confirmar-btn cancelar" onClick={() => onResponder(false)}>
            Cancelar
          </button>
          <button
            type="button"
            ref={botao}
            className={`confirmar-btn confirmar ${pedido.perigo ? 'perigo' : ''}`}
            onClick={() => onResponder(true)}
          >
            {pedido.rotulo}
          </button>
        </div>
      </div>
    </div>
  );
}
