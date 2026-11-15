import { useCallback, useState, type ReactNode } from 'react';
import { Dialogo, type Pedido } from './Confirmar';

/**
 * Substitui o window.confirm por um diálogo no estilo da aplicação.
 * Uso: const [confirmar, dialogo] = useConfirmar();  ...  if (!(await confirmar('Texto'))) return;
 * e renderize {dialogo} em algum ponto do JSX.
 */
export function useConfirmar() {
  const [pedido, setPedido] = useState<Pedido | null>(null);

  const confirmar = useCallback(
    (texto: string, opcoes: { rotulo?: string; perigo?: boolean } = {}) =>
      new Promise<boolean>((resolver) =>
        setPedido({ texto, rotulo: opcoes.rotulo ?? 'Confirmar', perigo: opcoes.perigo ?? false, resolver }),
      ),
    [],
  );

  const responder = useCallback(
    (confirmou: boolean) => {
      pedido?.resolver(confirmou);
      setPedido(null);
    },
    [pedido],
  );

  const dialogo: ReactNode = pedido ? <Dialogo pedido={pedido} onResponder={responder} /> : null;
  return [confirmar, dialogo] as const;
}
