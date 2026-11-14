import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Login } from './pages/Login';
import { CadastroUsuario } from './pages/CadastroUsuario';
import { Dashboard } from './pages/Dashboard';
import { Emprestimo } from "./pages/Emprestimo";
import { Devolucao } from "./pages/Devolucao";
import { Reserva } from './pages/Reserva';
import { Acervo } from './pages/Acervo';
import { FilaReserva } from './pages/FilaReserva';

export function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/cadastrar-usuario" element={<CadastroUsuario />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/emprestimo" element={<Emprestimo />} />
        <Route path="/devolucao" element={<Devolucao />} />
        <Route path="/reserva" element={<Reserva />} />
        <Route path="/acervo" element={<Acervo />} />
        <Route path="/filareserva" element={<FilaReserva />} />
        <Route path="*" element={<h2 style={{ padding: '2rem' }}>Página não encontrada</h2>} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;