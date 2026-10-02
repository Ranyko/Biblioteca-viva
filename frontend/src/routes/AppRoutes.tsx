import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';


import { Emprestimo } from '../pages/Emprestimo';
import { Devolucao } from '../pages/Devolucao';
import { FilaReserva } from '../pages/FilaReserva';
import { Reserva } from '../pages/Reserva';
import { Acervo } from '../pages/Acervo';

export function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>
       
        <Route path="/" element={<Navigate to="/emprestimo" replace />} />

       
        <Route path="/emprestimo" element={<Emprestimo />} />
        <Route path="/devolucao" element={<Devolucao />} />
        <Route path="/reserva" element={<FilaReserva />} />

      
        <Route path="/acervo" element={<Acervo />} />
        <Route path="/acervo/obra/:id" element={<Reserva />} />

     
        <Route path="*" element={<Navigate to="/emprestimo" replace />} />
      </Routes>
    </BrowserRouter>
  );
}