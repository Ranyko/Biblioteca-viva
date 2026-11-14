import { useState } from 'react';
import { Navbar } from '../components/Navbar';
import './FilaReserva.css';

interface Reserva {
  id: number;
  posicao: number;
  leitor: string;
  dataSolicitacao: string;
  status: 'Pronto para Retirada' | 'Aguardando' | 'Cancelado';
}

const RESERVAS_MOCK: Reserva[] = [
  {
    id: 1,
    posicao: 1,
    leitor: 'Carlos Andrade',
    dataSolicitacao: '11/08/26',
    status: 'Pronto para Retirada',
  },
  {
    id: 2,
    posicao: 2,
    leitor: 'Leticia Pavanote',
    dataSolicitacao: '12/08/26',
    status: 'Aguardando',
  },
  {
    id: 3,
    posicao: 3,
    leitor: 'Vinicius Jose',
    dataSolicitacao: '12/08/26',
    status: 'Aguardando',
  },
];

export function FilaReserva() {
  const [reservas, setReservas] = useState<Reserva[]>(RESERVAS_MOCK);

  const handleEdit = (id: number) => {
    alert(`Editar reserva #${id}`);
  };

  const handleDelete = (id: number) => {
    if (confirm('Deseja realmente remover esta reserva da fila?')) {
      const novasReservas = reservas
        .filter((r) => r.id !== id)
        .map((item, index) => ({ ...item, posicao: index + 1 })); // Recalcula a posição na fila
      setReservas(novasReservas);
    }
  };

  return (
    <div className="app-container">
      <Navbar activeTab="Reserva" userProfile={{ nome: 'Atendente' }} />

      <main className="reserva-container">
        <h1 className="page-title">Fila de Reserva</h1>

        <div className="reserva-card">
          <div className="table-responsive">
            <table className="reserva-table">
              <thead>
                <tr>
                  <th>Posição</th>
                  <th>Leitor</th>
                  <th>Data da Solicitação</th>
                  <th>Status</th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                {reservas.map((item) => (
                  <tr key={item.id}>
                    <td className="col-posicao">{item.posicao}</td>
                    <td className="col-leitor">{item.leitor}</td>
                    <td className="col-data">{item.dataSolicitacao}</td>
                    <td className="col-status">{item.status}</td>
                    <td className="col-acoes">
                      <button
                        type="button"
                        className="btn-icon"
                        title="Editar reserva"
                        onClick={() => handleEdit(item.id)}
                      >
                       
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <path d="M12 20h9" />
                          <path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z" />
                        </svg>
                      </button>

                      <button
                        type="button"
                        className="btn-icon btn-delete"
                        title="Remover reserva"
                        onClick={() => handleDelete(item.id)}
                      >
                        
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <polyline points="3 6 5 6 21 6" />
                          <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                        </svg>
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </main>
    </div>
  );
}